package com.valleblanco.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notas_cata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaCata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;
}
