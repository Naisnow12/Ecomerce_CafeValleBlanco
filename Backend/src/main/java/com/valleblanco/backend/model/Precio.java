package com.valleblanco.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "precios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Precio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    @Column(nullable = false)
    private Integer peso; // Ej: 250, 500, 1000 (en gramos)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @JsonProperty("id_product")
    public Long getIdProduct() {
        return product != null ? product.getId() : null;
    }
}
