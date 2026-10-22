package com.valleblanco.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String slug;

    @Column(name = "imagen_url", nullable = false)
    private String imagenUrl;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "nombre_secundario")
    private String nombreSecundario; // Ej: Clásico, Origen, Espresso, Reserva

    @Column(name = "tipo_cafe")
    private String tipoCafe; // Ej: Café Molido, Café en Grano, Espresso, Especialidad

    @Column(nullable = false)
    private String molienda; // Molido fino, Molido medio, Molido grueso, En Grano, Espresso

    @Column(nullable = false)
    private String tueste; // Medio, Medio claro, Claro, Oscuro, Medio-Oscuro

    @Column(nullable = false)
    private String variedad; // Caturra, Typica, Caturra / Typica, Geisha, Bourbon

    @Column(nullable = false)
    private String altitud; // 1,600 msnm, 1,800 msnm, etc.

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "es_destacado")
    @Builder.Default
    private Boolean esDestacado = false;

    @Column(name = "es_oscuro")
    @Builder.Default
    private Boolean esOscuro = false;

    @Column(name = "badge_text")
    private String badgeText; // Ej: "MÁS POPULAR", "EDICIÓN LIMITADA"

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(
        name = "producto_notas_cata",
        joinColumns = @JoinColumn(name = "producto_id"),
        inverseJoinColumns = @JoinColumn(name = "nota_cata_id")
    )
    @Builder.Default
    private List<NotaCata> notasCata = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<Precio> precios = new ArrayList<>();
}
