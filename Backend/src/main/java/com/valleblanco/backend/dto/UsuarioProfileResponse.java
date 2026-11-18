package com.valleblanco.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioProfileResponse {
    private Long id;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
}
