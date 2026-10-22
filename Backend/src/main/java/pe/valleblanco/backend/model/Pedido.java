package pe.valleblanco.backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_pedido")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    public Usuario usuario;
    @Column(name = "creado_en", nullable = false)
    public Instant creadoEn = Instant.now();
    @Column(nullable = false, length = 30)
    public String estado = "PENDIENTE_PAGO";
    @Column(name = "metodo_pago", nullable = false, length = 25)
    public String metodoPago;
    @Column(name = "direccion_envio", nullable = false, length = 500)
    public String direccionEnvio;
    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal total;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PedidoDetalle> detalles = new ArrayList<>();
}
