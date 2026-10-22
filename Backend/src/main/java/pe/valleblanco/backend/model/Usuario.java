package pe.valleblanco.backend.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, length = 120)
    public String nombre;
    @Column(nullable = false, unique = true, length = 254)
    public String email;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 100)
    public String passwordHash;
    @Column(nullable = false, length = 20)
    public String rol = "CLIENTE";
    @Column(nullable = false)
    public boolean activo = true;
    @Column(name = "creado_en", nullable = false)
    public Instant creadoEn = Instant.now();
}
