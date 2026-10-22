package pe.valleblanco.backend.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    public Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria", nullable = false)
    public Categoria categoria;
    @Column(nullable = false, length = 150)
    public String nombre;
    @Column(columnDefinition = "text")
    public String descripcion;
    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal precio;
    @Column(nullable = false)
    public Integer stock = 0;
    @Column(name = "tipo_preparado", length = 100)
    public String tipoPreparado;
    @Column(name = "imagen_url", columnDefinition = "text")
    public String imagenUrl;
    @Column(nullable = false)
    public Boolean destacado = false;
    @Column(nullable = false)
    public Boolean estado = true;
}
