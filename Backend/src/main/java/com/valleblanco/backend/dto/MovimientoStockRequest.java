package com.valleblanco.backend.dto;

import lombok.Data;

@Data
public class MovimientoStockRequest {
    private Long productoId;
    private String tipo; // ENTRADA, SALIDA, AJUSTE
    private Integer cantidad;
    private String motivo;
}
