package com.valleblanco.backend.dto;

import lombok.Data;

@Data
public class CrearPedidoRequest {
    private Long direccionId;
    private String metodoPago;
    private String notas;
}
