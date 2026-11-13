package com.valleblanco.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemResponse {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private String nombreSecundario;
    private String imagenUrl;
    private Integer peso;
    private BigDecimal precioUnitario;
    private Integer cantidad;
    private BigDecimal subtotalItem;
}
