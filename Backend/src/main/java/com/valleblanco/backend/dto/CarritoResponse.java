package com.valleblanco.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoResponse {
    private Long id;
    private List<CarritoItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal total;
}
