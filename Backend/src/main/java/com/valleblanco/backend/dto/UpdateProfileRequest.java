package com.valleblanco.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Los nombres son requeridos")
    @Size(max = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son requeridos")
    @Size(max = 100)
    private String apellidos;

    @Size(max = 20)
    private String telefono;
}
