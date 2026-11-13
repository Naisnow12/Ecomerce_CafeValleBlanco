package com.valleblanco.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "carrito_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"carrito_id", "precio_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "precio_id", nullable = false)
    private Precio precio;

    @Column(nullable = false)
    @Builder.Default
    private Integer cantidad = 1;
}
