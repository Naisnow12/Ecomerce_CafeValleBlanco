package com.valleblanco.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireccionResponse {
    private Long id;
    private String alias;
    private String destinatario;
    private String linea1;
    private String linea2;
    private String distrito;
    private String provincia;
    private String departamento;
    private String codigoPostal;
    private String telefono;
    private Boolean esPrincipal;
}
