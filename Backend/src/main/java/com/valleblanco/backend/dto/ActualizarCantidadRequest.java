package com.valleblanco.backend.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ActualizarCantidadRequest {

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int cantidad;
}
