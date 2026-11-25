package com.valleblanco.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteMetricasResponse {
    private String periodo;
    private String rangoTexto;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    // Métricas clave
    private BigDecimal totalIngresos;
    private long totalPedidos;
    private long pedidosCompletados;
    private long pedidosPendientes;
    private long pedidosCancelados;
    private BigDecimal ticketPromedio;
    private int totalUnidadesVendidas;
    private long totalClientesCompradores;

    // Desgloses
    private Map<String, Long> pedidosPorEstado;
    private Map<String, BigDecimal> ventasPorMetodoPago;
    private List<TopProductoReporte> topProductos;
    private List<VentaDepartamentoReporte> ventasPorDepartamento;
    private List<StockCriticoReporte> stockCritico;
    private List<PedidoResumenReporte> pedidos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VentaDepartamentoReporte {
        private String departamento;
        private long cantidadPedidos;
        private int unidadesVendidas;
        private BigDecimal totalGenerado;
        private double porcentaje;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductoReporte {
        private String nombre;
        private int unidadesVendidas;
        private BigDecimal totalGenerado;
        private double porcentaje;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockCriticoReporte {
        private Long id;
        private String nombre;
        private Integer stockActual;
        private Integer nivelMinimo;
        private String estado;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PedidoResumenReporte {
        private Long id;
        private String numeroPedido;
        private String cliente;
        private LocalDateTime fecha;
        private String estado;
        private String metodoPago;
        private BigDecimal total;
    }
}
