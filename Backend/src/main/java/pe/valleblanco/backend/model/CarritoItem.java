package pe.valleblanco.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "app_carrito_item", uniqueConstraints = @UniqueConstraint(columnNames = { "usuario_id", "producto_id" }))
public class CarritoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    public Usuario usuario;
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    public Producto producto;
    @Column(nullable = false)
    public int cantidad;
}
