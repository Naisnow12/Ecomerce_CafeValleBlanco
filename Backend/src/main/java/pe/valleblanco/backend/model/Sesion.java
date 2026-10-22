package pe.valleblanco.backend.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_sesion")
public class Sesion {
    @Id
    @Column(name = "token_hash", length = 64)
    public String tokenHash;
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    public Usuario usuario;
    @Column(name = "expira_en", nullable = false)
    public Instant expiraEn;
}
