package com.valleblanco.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Los nombres son requeridos")
    @Size(max = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son requeridos")
    @Size(max = 100)
    private String apellidos;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email inválido")
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String telefono;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "La confirmación de contraseña es requerida")
    private String confirmPassword;
}
