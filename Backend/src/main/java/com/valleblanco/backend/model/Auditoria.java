package com.valleblanco.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_usuario", nullable = false, length = 150)
    private String emailUsuario;

    @Column(name = "rol_usuario", length = 100)
    private String rolUsuario;

    @Column(nullable = false, length = 100)
    private String accion;

    @Column(nullable = false, length = 100)
    private String modulo;

    @Column(name = "id_registro", length = 100)
    private String idRegistro;

    @Column(columnDefinition = "TEXT")
    private String detalles;

    @Column(name = "ip_origen", length = 50)
    private String ipOrigen;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String resultado = "EXITO";

    @Column(name = "creado_en", nullable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();
}
