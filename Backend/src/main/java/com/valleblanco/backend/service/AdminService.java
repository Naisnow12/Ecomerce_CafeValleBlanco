package com.valleblanco.backend.service;

import com.valleblanco.backend.dto.PedidoItemResponse;
import com.valleblanco.backend.dto.PedidoResponse;
import com.valleblanco.backend.model.Pedido;
import com.valleblanco.backend.model.Product;
import com.valleblanco.backend.model.Role;
import com.valleblanco.backend.model.Usuario;
import com.valleblanco.backend.repository.PedidoRepository;
import com.valleblanco.backend.repository.ProductRepository;
import com.valleblanco.backend.repository.RolRepository;
import com.valleblanco.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductRepository productRepository;
    private final AuditoriaService auditoriaService;

    // --- Dashboard Stats ---
    public Map<String, Object> getDashboardStats() {
        List<Product> productos = productRepository.findAll();
        List<Pedido> pedidos = pedidoRepository.findAll();
        List<Usuario> usuarios = usuarioRepository.findAll();

        long totalProductos = productos.size();
        long totalUsuarios = usuarios.size();
        long totalPedidos = pedidos.size();

        BigDecimal ingresosTotales = pedidos.stream()
                .filter(p -> !"CANCELADO".equalsIgnoreCase(p.getEstado()))
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pedidosPendientes = pedidos.stream()
                .filter(p -> "PENDIENTE".equalsIgnoreCase(p.getEstado()) || "CONFIRMADO".equalsIgnoreCase(p.getEstado()))
                .count();

        long stockBajo = productos.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= (p.getNivelMinimoStock() != null ? p.getNivelMinimoStock() : 10))
                .count();

        // Top 5 productos más vendidos calculados a partir de los items de pedidos no cancelados
        Map<String, Integer> ventasPorProducto = new HashMap<>();
        pedidos.stream()
                .filter(p -> !"CANCELADO".equalsIgnoreCase(p.getEstado()))
                .flatMap(p -> p.getItems().stream())
                .forEach(item -> {
                    String nombre = item.getNombreProducto() + (item.getNombreSecundario() != null ? " " + item.getNombreSecundario() : "");
                    ventasPorProducto.put(nombre, ventasPorProducto.getOrDefault(nombre, 0) + item.getCantidad());
                });

        List<Map<String, Object>> topProductos = ventasPorProducto.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(e -> Map.<String, Object>of("nombre", e.getKey(), "cantidad", e.getValue()))
                .toList();

        // Últimos 5 pedidos
        List<PedidoResponse> ultimosPedidos = pedidos.stream()
                .sorted((a, b) -> b.getCreadoEn().compareTo(a.getCreadoEn()))
                .limit(5)
                .map(this::toPedidoResponse)
                .toList();

        return Map.of(
                "totalProductos", totalProductos,
                "totalUsuarios", totalUsuarios,
                "totalPedidos", totalPedidos,
                "ingresosTotales", ingresosTotales,
                "pedidosPendientes", pedidosPendientes,
                "stockBajo", stockBajo,
                "topProductos", topProductos,
                "ultimosPedidos", ultimosPedidos
        );
    }

    // --- Gestión de Usuarios ---
    public List<Map<String, Object>> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "nombres", u.getNombres(),
                        "apellidos", u.getApellidos(),
                        "email", u.getEmail(),
                        "telefono", u.getTelefono() != null ? u.getTelefono() : "",
                        "activo", u.getActivo(),
                        "roles", u.getRoles().stream().map(Role::getNombre).toList(),
                        "creadoEn", u.getCreadoEn()
                ))
                .toList();
    }

    @Transactional
    public void cambiarEstadoUsuario(Long usuarioId, boolean activo, String adminEmail) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (u.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new IllegalArgumentException("No puedes desactivar tu propia cuenta administradora");
        }

        u.setActivo(activo);
        u.setActualizadoEn(LocalDateTime.now());
        usuarioRepository.save(u);

        auditoriaService.registrar(
                adminEmail,
                "ROLE_ADMIN",
                activo ? "ACTIVAR_USUARIO" : "DESACTIVAR_USUARIO",
                "USUARIOS",
                u.getId().toString(),
                "Usuario " + u.getEmail() + " estado cambiado a " + (activo ? "ACTIVO" : "INACTIVO"),
                "EXITO"
        );
    }

    @Transactional
    public void asignarRoles(Long usuarioId, List<String> nuevosRoles, String adminEmail) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        List<Role> rolesList = new ArrayList<>();
        for (String rName : nuevosRoles) {
            Role r = rolRepository.findByNombre(rName)
                    .orElseThrow(() -> new IllegalArgumentException("Rol no válido: " + rName));
            rolesList.add(r);
        }

        u.setRoles(rolesList);
        u.setActualizadoEn(LocalDateTime.now());
        usuarioRepository.save(u);

        auditoriaService.registrar(
                adminEmail,
                "ROLE_ADMIN",
                "CAMBIAR_ROLES",
                "USUARIOS",
                u.getId().toString(),
                "Nuevos roles para " + u.getEmail() + ": " + String.join(", ", nuevosRoles),
                "EXITO"
        );
    }

    // --- Gestión de Pedidos ---
    public List<PedidoResponse> listarTodosLosPedidos() {
        return pedidoRepository.findAll().stream()
                .sorted((a, b) -> b.getCreadoEn().compareTo(a.getCreadoEn()))
                .map(this::toPedidoResponse)
                .toList();
    }

    @Transactional
    public PedidoResponse actualizarEstadoPedido(Long pedidoId, String nuevoEstado, String adminEmail) {
        Pedido p = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado: " + pedidoId));

        String estadoAnterior = p.getEstado();
        p.setEstado(nuevoEstado.toUpperCase());
        p.setActualizadoEn(LocalDateTime.now());
        Pedido saved = pedidoRepository.save(p);

        auditoriaService.registrar(
                adminEmail,
                "ROLE_ADMIN",
                "ACTUALIZAR_ESTADO_PEDIDO",
                "PEDIDOS",
                p.getId().toString(),
                "Pedido " + p.getNumeroPedido() + ": estado " + estadoAnterior + " -> " + nuevoEstado,
                "EXITO"
        );

        return toPedidoResponse(saved);
    }

    private PedidoResponse toPedidoResponse(Pedido pedido) {
        List<PedidoItemResponse> items = pedido.getItems().stream()
                .map(item -> PedidoItemResponse.builder()
                        .productoId(item.getProductoId())
                        .nombreProducto(item.getNombreProducto())
                        .nombreSecundario(item.getNombreSecundario())
                        .peso(item.getPeso())
                        .precioUnitario(item.getPrecioUnitario())
                        .cantidad(item.getCantidad())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return PedidoResponse.builder()
                .id(pedido.getId())
                .numeroPedido(pedido.getNumeroPedido())
                .estado(pedido.getEstado())
                .items(items)
                .subtotal(pedido.getSubtotal())
                .costoEnvio(pedido.getCostoEnvio())
                .total(pedido.getTotal())
                .metodoPago(pedido.getMetodoPago())
                .notas(pedido.getNotas())
                .creadoEn(pedido.getCreadoEn())
                .dirLinea1(pedido.getDirLinea1())
                .dirLinea2(pedido.getDirLinea2())
                .dirDistrito(pedido.getDirDistrito())
                .dirProvincia(pedido.getDirProvincia())
                .dirDepartamento(pedido.getDirDepartamento())
                .dirDestinatario(pedido.getDirDestinatario())
                .dirTelefono(pedido.getDirTelefono())
                .build();
    }
}
