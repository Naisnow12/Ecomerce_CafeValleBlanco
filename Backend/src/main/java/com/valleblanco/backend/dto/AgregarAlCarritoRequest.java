package com.valleblanco.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgregarAlCarritoRequest {

    @NotNull(message = "El precioId es requerido")
    private Long precioId;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int cantidad = 1;
}
