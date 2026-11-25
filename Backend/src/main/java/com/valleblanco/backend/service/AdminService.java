package com.valleblanco.backend.service;

import com.valleblanco.backend.dto.PedidoItemResponse;
import com.valleblanco.backend.dto.PedidoResponse;
import com.valleblanco.backend.dto.ReporteMetricasResponse;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        Map<Long, String> productImages = obtenerMapaImagenesProductos();
        List<PedidoResponse> ultimosPedidos = pedidos.stream()
                .sorted((a, b) -> b.getCreadoEn().compareTo(a.getCreadoEn()))
                .limit(5)
                .map(p -> toPedidoResponse(p, productImages))
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
        List<Pedido> pedidos = pedidoRepository.findAll();
        Map<Long, Long> pedidosPorUsuario = pedidos.stream()
                .filter(p -> p.getUsuario() != null)
                .collect(Collectors.groupingBy(p -> p.getUsuario().getId(), Collectors.counting()));

        Map<Long, BigDecimal> comprasPorUsuario = pedidos.stream()
                .filter(p -> p.getUsuario() != null && !"CANCELADO".equalsIgnoreCase(p.getEstado()))
                .collect(Collectors.groupingBy(
                        p -> p.getUsuario().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Pedido::getTotal, BigDecimal::add)
                ));

        return usuarioRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", u.getId());
                    map.put("nombres", u.getNombres());
                    map.put("apellidos", u.getApellidos());
                    map.put("email", u.getEmail());
                    map.put("telefono", u.getTelefono() != null ? u.getTelefono() : "");
                    map.put("activo", u.getActivo());
                    map.put("roles", u.getRoles().stream().map(Role::getNombre).toList());
                    map.put("creadoEn", u.getCreadoEn());
                    map.put("totalPedidos", pedidosPorUsuario.getOrDefault(u.getId(), 0L));
                    map.put("totalGastado", comprasPorUsuario.getOrDefault(u.getId(), BigDecimal.ZERO));
                    return map;
                })
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
        Map<Long, String> productImages = obtenerMapaImagenesProductos();
        return pedidoRepository.findAll().stream()
                .sorted((a, b) -> b.getCreadoEn().compareTo(a.getCreadoEn()))
                .map(p -> toPedidoResponse(p, productImages))
                .toList();
    }

    public PedidoResponse obtenerPedidoPorId(Long pedidoId) {
        Pedido p = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado: " + pedidoId));
        return toPedidoResponse(p);
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

    private Map<Long, String> obtenerMapaImagenesProductos() {
        return productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getId, Product::getImagenUrl, (a, b) -> a));
    }

    private PedidoResponse toPedidoResponse(Pedido pedido) {
        return toPedidoResponse(pedido, obtenerMapaImagenesProductos());
    }

    private PedidoResponse toPedidoResponse(Pedido pedido, Map<Long, String> productImages) {
        List<PedidoItemResponse> items = pedido.getItems().stream()
                .map(item -> PedidoItemResponse.builder()
                        .productoId(item.getProductoId())
                        .nombreProducto(item.getNombreProducto())
                        .nombreSecundario(item.getNombreSecundario())
                        .peso(item.getPeso())
                        .precioUnitario(item.getPrecioUnitario())
                        .cantidad(item.getCantidad())
                        .subtotal(item.getSubtotal())
                        .imagenUrl(productImages.get(item.getProductoId()))
                        .build())
                .toList();

        String clienteEmail = pedido.getUsuario() != null ? pedido.getUsuario().getEmail() : null;
        String clienteNombre = pedido.getUsuario() != null
                ? pedido.getUsuario().getNombres() + " " + pedido.getUsuario().getApellidos()
                : pedido.getDirDestinatario();

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
                .clienteEmail(clienteEmail)
                .clienteNombre(clienteNombre)
                .build();
    }

    // --- Reportes & Métricas Avanzadas ---
    public ReporteMetricasResponse generarReporteMetricas(
            String periodo,
            String estadoFiltro,
            String metodoPagoFiltro,
            String fechaInicioStr,
            String fechaFinStr
    ) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicio = null;
        LocalDateTime fin = ahora;
        String rangoTexto = "Histórico General";

        String p = (periodo != null) ? periodo.toUpperCase().trim() : "HISTORICO";

        switch (p) {
            case "HOY":
                inicio = ahora.toLocalDate().atStartOfDay();
                rangoTexto = "Hoy (" + ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";
                break;
            case "7_DIAS":
                inicio = ahora.minusDays(7);
                rangoTexto = "Últimos 7 días";
                break;
            case "ESTE_MES":
                inicio = ahora.withDayOfMonth(1).toLocalDate().atStartOfDay();
                rangoTexto = "Este mes (" + ahora.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-PE"))) + ")";
                break;
            case "30_DIAS":
                inicio = ahora.minusDays(30);
                rangoTexto = "Últimos 30 días";
                break;
            case "ESTE_ANIO":
                inicio = ahora.withDayOfYear(1).toLocalDate().atStartOfDay();
                rangoTexto = "Año " + ahora.getYear();
                break;
            case "PERSONALIZADO":
                try {
                    if (fechaInicioStr != null && !fechaInicioStr.isBlank()) {
                        inicio = LocalDate.parse(fechaInicioStr).atStartOfDay();
                    }
                    if (fechaFinStr != null && !fechaFinStr.isBlank()) {
                        fin = LocalDate.parse(fechaFinStr).atTime(23, 59, 59);
                    }
                    rangoTexto = (inicio != null ? inicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Inicio") +
                            " al " + fin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } catch (Exception e) {
                    inicio = null;
                    rangoTexto = "Rango personalizado (Todos)";
                }
                break;
            default:
                inicio = null;
                rangoTexto = "Todo el historial";
                break;
        }

        final LocalDateTime fInicio = inicio;
        final LocalDateTime fFin = fin;

        List<Pedido> todosPedidos = pedidoRepository.findAll();

        // Filtrado por fecha, estado y método de pago
        List<Pedido> pedidosFiltrados = todosPedidos.stream()
                .filter(ped -> fInicio == null || !ped.getCreadoEn().isBefore(fInicio))
                .filter(ped -> fFin == null || !ped.getCreadoEn().isAfter(fFin))
                .filter(ped -> estadoFiltro == null || estadoFiltro.isBlank() || "TODOS".equalsIgnoreCase(estadoFiltro) || ped.getEstado().equalsIgnoreCase(estadoFiltro))
                .filter(ped -> metodoPagoFiltro == null || metodoPagoFiltro.isBlank() || "TODOS".equalsIgnoreCase(metodoPagoFiltro) || (ped.getMetodoPago() != null && ped.getMetodoPago().equalsIgnoreCase(metodoPagoFiltro)))
                .sorted((a, b) -> b.getCreadoEn().compareTo(a.getCreadoEn()))
                .toList();

        // Cálculos financieros y volumétricos
        List<Pedido> pedidosValidos = pedidosFiltrados.stream()
                .filter(ped -> !"CANCELADO".equalsIgnoreCase(ped.getEstado()))
                .toList();

        BigDecimal totalIngresos = pedidosValidos.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalPedidos = pedidosFiltrados.size();
        long pedidosCompletados = pedidosFiltrados.stream()
                .filter(ped -> "ENTREGADO".equalsIgnoreCase(ped.getEstado()) || "ENVIADO".equalsIgnoreCase(ped.getEstado()))
                .count();
        long pedidosPendientes = pedidosFiltrados.stream()
                .filter(ped -> "PENDIENTE".equalsIgnoreCase(ped.getEstado()) || "CONFIRMADO".equalsIgnoreCase(ped.getEstado()) || "EN_PREPARACION".equalsIgnoreCase(ped.getEstado()))
                .count();
        long pedidosCancelados = pedidosFiltrados.stream()
                .filter(ped -> "CANCELADO".equalsIgnoreCase(ped.getEstado()))
                .count();

        long pedidosParaTicket = totalPedidos - pedidosCancelados;
        BigDecimal ticketPromedio = (pedidosParaTicket > 0 && totalIngresos.compareTo(BigDecimal.ZERO) > 0)
                ? totalIngresos.divide(BigDecimal.valueOf(pedidosParaTicket), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        int totalUnidadesVendidas = pedidosValidos.stream()
                .flatMap(ped -> ped.getItems().stream())
                .mapToInt(item -> item.getCantidad() != null ? item.getCantidad() : 0)
                .sum();

        long totalClientesCompradores = pedidosValidos.stream()
                .map(ped -> ped.getUsuario() != null ? ped.getUsuario().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        // Desglose por estado
        Map<String, Long> pedidosPorEstado = new LinkedHashMap<>();
        for (String est : List.of("PENDIENTE", "CONFIRMADO", "EN_PREPARACION", "ENVIADO", "ENTREGADO", "CANCELADO")) {
            long count = pedidosFiltrados.stream().filter(ped -> est.equalsIgnoreCase(ped.getEstado())).count();
            if (count > 0 || "TODOS".equalsIgnoreCase(estadoFiltro) || estadoFiltro == null) {
                pedidosPorEstado.put(est, count);
            }
        }

        // Desglose por método de pago
        Map<String, BigDecimal> ventasPorMetodoPago = new LinkedHashMap<>();
        pedidosValidos.forEach(ped -> {
            String mPago = ped.getMetodoPago() != null ? ped.getMetodoPago().toUpperCase() : "NO ESPECIFICADO";
            ventasPorMetodoPago.put(mPago, ventasPorMetodoPago.getOrDefault(mPago, BigDecimal.ZERO).add(ped.getTotal()));
        });

        // Top Productos
        Map<String, int[]> ventasPorProd = new HashMap<>(); // [unidades, centavosTotal]
        pedidosValidos.forEach(ped -> {
            ped.getItems().forEach(item -> {
                String nombre = item.getNombreProducto() + (item.getNombreSecundario() != null && !item.getNombreSecundario().isBlank() ? " " + item.getNombreSecundario() : "");
                int cant = item.getCantidad() != null ? item.getCantidad() : 0;
                BigDecimal sub = item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO;
                int centavos = sub.multiply(BigDecimal.valueOf(100)).intValue();

                ventasPorProd.computeIfAbsent(nombre, k -> new int[]{0, 0});
                ventasPorProd.get(nombre)[0] += cant;
                ventasPorProd.get(nombre)[1] += centavos;
            });
        });

        BigDecimal ingresosParaPorcentaje = totalIngresos.compareTo(BigDecimal.ZERO) > 0 ? totalIngresos : BigDecimal.ONE;
        List<ReporteMetricasResponse.TopProductoReporte> topProductos = ventasPorProd.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]))
                .limit(8)
                .map(e -> {
                    BigDecimal subtotalProd = BigDecimal.valueOf(e.getValue()[1]).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    double porc = subtotalProd.divide(ingresosParaPorcentaje, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                    return ReporteMetricasResponse.TopProductoReporte.builder()
                            .nombre(e.getKey())
                            .unidadesVendidas(e.getValue()[0])
                            .totalGenerado(subtotalProd)
                            .porcentaje(Math.min(100.0, porc))
                            .build();
                })
                .toList();

        // Ventas por Departamento
        Map<String, Object[]> ventasDeptoMap = new HashMap<>(); // [String nombreNormalizado, long pedidos, int unidades, BigDecimal total]
        pedidosValidos.forEach(ped -> {
            String rawDepto = ped.getDirDepartamento();
            String depto = (rawDepto != null && !rawDepto.isBlank()) ? rawDepto.trim() : "No especificado";
            String key = depto.toUpperCase(Locale.ROOT);
            int unidadesPed = ped.getItems().stream()
                    .mapToInt(item -> item.getCantidad() != null ? item.getCantidad() : 0)
                    .sum();
            BigDecimal totalPed = ped.getTotal() != null ? ped.getTotal() : BigDecimal.ZERO;

            ventasDeptoMap.computeIfAbsent(key, k -> new Object[]{depto, 0L, 0, BigDecimal.ZERO});
            Object[] acc = ventasDeptoMap.get(key);
            acc[1] = (Long) acc[1] + 1L;
            acc[2] = (Integer) acc[2] + unidadesPed;
            acc[3] = ((BigDecimal) acc[3]).add(totalPed);
        });

        List<ReporteMetricasResponse.VentaDepartamentoReporte> ventasPorDepartamento = ventasDeptoMap.values().stream()
                .sorted((a, b) -> ((BigDecimal) b[3]).compareTo((BigDecimal) a[3]))
                .map(acc -> {
                    BigDecimal totalDepto = (BigDecimal) acc[3];
                    double porc = totalDepto.divide(ingresosParaPorcentaje, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                    return ReporteMetricasResponse.VentaDepartamentoReporte.builder()
                            .departamento((String) acc[0])
                            .cantidadPedidos((Long) acc[1])
                            .unidadesVendidas((Integer) acc[2])
                            .totalGenerado(totalDepto)
                            .porcentaje(Math.min(100.0, porc))
                            .build();
                })
                .toList();

        // Alertas de Stock
        List<Product> todosProductos = productRepository.findAll();
        List<ReporteMetricasResponse.StockCriticoReporte> stockCritico = todosProductos.stream()
                .filter(pr -> pr.getStock() != null && pr.getStock() <= (pr.getNivelMinimoStock() != null ? pr.getNivelMinimoStock() : 10))
                .sorted(Comparator.comparingInt(Product::getStock))
                .map(pr -> {
                    int nivelMin = pr.getNivelMinimoStock() != null ? pr.getNivelMinimoStock() : 10;
                    String estado = pr.getStock() <= 0 ? "AGOTADO" : (pr.getStock() <= 5 ? "CRÍTICO" : "BAJO");
                    return ReporteMetricasResponse.StockCriticoReporte.builder()
                            .id(pr.getId())
                            .nombre(pr.getNombre() + (pr.getNombreSecundario() != null ? " " + pr.getNombreSecundario() : ""))
                            .stockActual(pr.getStock())
                            .nivelMinimo(nivelMin)
                            .estado(estado)
                            .build();
                })
                .toList();

        // Resumen de pedidos para tabla
        List<ReporteMetricasResponse.PedidoResumenReporte> pedidosResumen = pedidosFiltrados.stream()
                .map(ped -> ReporteMetricasResponse.PedidoResumenReporte.builder()
                        .id(ped.getId())
                        .numeroPedido(ped.getNumeroPedido())
                        .cliente(ped.getDirDestinatario() != null ? ped.getDirDestinatario() : (ped.getUsuario() != null ? ped.getUsuario().getNombres() + " " + ped.getUsuario().getApellidos() : "Cliente"))
                        .fecha(ped.getCreadoEn())
                        .estado(ped.getEstado())
                        .metodoPago(ped.getMetodoPago())
                        .total(ped.getTotal())
                        .build())
                .toList();

        return ReporteMetricasResponse.builder()
                .periodo(p)
                .rangoTexto(rangoTexto)
                .fechaInicio(fInicio)
                .fechaFin(fFin)
                .totalIngresos(totalIngresos)
                .totalPedidos(totalPedidos)
                .pedidosCompletados(pedidosCompletados)
                .pedidosPendientes(pedidosPendientes)
                .pedidosCancelados(pedidosCancelados)
                .ticketPromedio(ticketPromedio)
                .totalUnidadesVendidas(totalUnidadesVendidas)
                .totalClientesCompradores(totalClientesCompradores)
                .pedidosPorEstado(pedidosPorEstado)
                .ventasPorMetodoPago(ventasPorMetodoPago)
                .topProductos(topProductos)
                .ventasPorDepartamento(ventasPorDepartamento)
                .stockCritico(stockCritico)
                .pedidos(pedidosResumen)
                .build();
    }
}
