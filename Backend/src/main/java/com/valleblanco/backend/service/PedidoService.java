package com.valleblanco.backend.service;

import com.valleblanco.backend.dto.CrearPedidoRequest;
import com.valleblanco.backend.dto.PedidoItemResponse;
import com.valleblanco.backend.dto.PedidoResponse;
import com.valleblanco.backend.model.*;
import com.valleblanco.backend.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final DireccionRepository direccionRepository;
    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    private Usuario findUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + email));
    }

    @Transactional
    public PedidoResponse crearPedido(String email, CrearPedidoRequest request) {
        Usuario usuario = findUsuario(email);

        // Obtener carrito
        Carrito carrito = carritoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("El carrito está vacío"));

        if (carrito.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        // Verificar stock de todos los items antes de proceder
        for (CarritoItem item : carrito.getItems()) {
            Product producto = item.getPrecio().getProduct();
            if (item.getCantidad() > producto.getStock()) {
                throw new IllegalArgumentException(
                    "Stock insuficiente para \"" + producto.getNombre() + " " + producto.getNombreSecundario()
                    + "\". Disponible: " + producto.getStock() + ", solicitado: " + item.getCantidad()
                );
            }
        }

        // Obtener dirección snapshot
        Direccion direccion = null;
        if (request.getDireccionId() != null) {
            direccion = direccionRepository.findById(request.getDireccionId())
                    .orElseThrow(() -> new EntityNotFoundException("Dirección no encontrada: " + request.getDireccionId()));
            if (!direccion.getUsuario().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("La dirección no pertenece al usuario");
            }
        }

        // Generar número de pedido
        Long seq = (Long) entityManager.createNativeQuery("SELECT nextval('seq_numero_pedido')")
                .getSingleResult();
        String numeroPedido = String.format("VB-%d-%05d", Year.now().getValue(), seq);

        // Calcular subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        List<PedidoItem> pedidoItems = new ArrayList<>();

        for (CarritoItem item : carrito.getItems()) {
            Precio precio = item.getPrecio();
            Product producto = precio.getProduct();
            BigDecimal itemSubtotal = precio.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            subtotal = subtotal.add(itemSubtotal);

            PedidoItem pedidoItem = PedidoItem.builder()
                    .productoId(producto.getId())
                    .nombreProducto(producto.getNombre())
                    .nombreSecundario(producto.getNombreSecundario())
                    .peso(precio.getPeso())
                    .precioUnitario(precio.getPrecio())
                    .cantidad(item.getCantidad())
                    .subtotal(itemSubtotal)
                    .build();

            pedidoItems.add(pedidoItem);
        }

        BigDecimal costoEnvio = BigDecimal.ZERO; // Envío gratis por ahora
        BigDecimal total = subtotal.add(costoEnvio);

        // Crear pedido
        Pedido pedido = Pedido.builder()
                .numeroPedido(numeroPedido)
                .usuario(usuario)
                .estado("PENDIENTE")
                .subtotal(subtotal)
                .costoEnvio(costoEnvio)
                .total(total)
                .metodoPago(request.getMetodoPago())
                .notas(request.getNotas())
                .creadoEn(LocalDateTime.now())
                .actualizadoEn(LocalDateTime.now())
                .dirLinea1(direccion != null ? direccion.getLinea1() : null)
                .dirLinea2(direccion != null ? direccion.getLinea2() : null)
                .dirDistrito(direccion != null ? direccion.getDistrito() : null)
                .dirProvincia(direccion != null ? direccion.getProvincia() : null)
                .dirDepartamento(direccion != null ? direccion.getDepartamento() : null)
                .dirDestinatario(direccion != null ? direccion.getDestinatario() : null)
                .dirTelefono(direccion != null ? direccion.getTelefono() : null)
                .items(pedidoItems)
                .build();

        // Establecer la referencia al pedido en cada item
        for (PedidoItem item : pedidoItems) {
            item.setPedido(pedido);
        }

        // Reducir stock
        for (CarritoItem item : carrito.getItems()) {
            Product producto = item.getPrecio().getProduct();
            producto.setStock(producto.getStock() - item.getCantidad());
            productRepository.save(producto);
        }

        // Guardar pedido
        Pedido savedPedido = pedidoRepository.save(pedido);

        // Vaciar carrito
        carrito.getItems().clear();
        carrito.setActualizadoEn(LocalDateTime.now());
        carritoRepository.save(carrito);

        return toPedidoResponse(savedPedido);
    }

    public List<PedidoResponse> getMisPedidos(String email) {
        Usuario usuario = findUsuario(email);
        return pedidoRepository.findByUsuarioIdOrderByCreadoEnDesc(usuario.getId())
                .stream()
                .map(this::toPedidoResponse)
                .toList();
    }

    public PedidoResponse getDetallePedido(String email, Long pedidoId) {
        Usuario usuario = findUsuario(email);
        Pedido pedido = pedidoRepository.findByIdAndUsuarioId(pedidoId, usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado: " + pedidoId));
        return toPedidoResponse(pedido);
    }

    // --- Helpers ---

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
