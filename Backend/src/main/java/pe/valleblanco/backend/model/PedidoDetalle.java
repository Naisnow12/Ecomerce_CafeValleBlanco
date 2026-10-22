package pe.valleblanco.backend.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_pedido_detalle")
public class PedidoDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    public Pedido pedido;
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    public Producto producto;
    @Column(nullable = false)
    public Integer cantidad;
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    public BigDecimal precioUnitario;
    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal subtotal;
}
