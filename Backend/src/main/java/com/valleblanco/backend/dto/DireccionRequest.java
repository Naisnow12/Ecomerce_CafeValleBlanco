package com.valleblanco.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DireccionRequest {

    @Size(max = 60)
    private String alias;

    @Size(max = 150)
    private String destinatario;

    @NotBlank(message = "La línea 1 es requerida")
    @Size(max = 255)
    private String linea1;

    @Size(max = 255)
    private String linea2;

    @NotBlank(message = "El distrito es requerido")
    @Size(max = 100)
    private String distrito;

    @NotBlank(message = "La provincia es requerida")
    @Size(max = 100)
    private String provincia;

    @NotBlank(message = "El departamento es requerido")
    @Size(max = 100)
    private String departamento;

    @Size(max = 10)
    private String codigoPostal;

    @Size(max = 20)
    private String telefono;

    private Boolean esPrincipal = false;
}
