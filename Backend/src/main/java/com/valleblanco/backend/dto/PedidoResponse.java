package com.valleblanco.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponse {
    private Long id;
    private String numeroPedido;
    private String estado;
    private List<PedidoItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal costoEnvio;
    private BigDecimal total;
    private String metodoPago;
    private String notas;
    private LocalDateTime creadoEn;
    // Dirección snapshot
    private String dirLinea1;
    private String dirLinea2;
    private String dirDistrito;
    private String dirProvincia;
    private String dirDepartamento;
    private String dirDestinatario;
    private String dirTelefono;
}
