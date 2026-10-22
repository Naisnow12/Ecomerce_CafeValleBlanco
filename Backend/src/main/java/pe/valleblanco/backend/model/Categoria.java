package pe.valleblanco.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categoria")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    public Long id;
    @Column(nullable = false, unique = true, length = 100)
    public String nombre;
    @Column(length = 255)
    public String descripcion;

    public Categoria() {
    }

    public Categoria(String n, String d) {
        nombre = n;
        descripcion = d;
    }
}
