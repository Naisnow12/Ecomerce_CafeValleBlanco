
package pe.valleblanco.backend.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import pe.valleblanco.backend.model.Producto;

public interface ProductoRepo extends JpaRepository<Producto, Long> {

    // Listar productos activos y buscar por nombre
    @Query("""
            SELECT p FROM Producto p
            WHERE p.estado = true
            AND LOWER(p.nombre) LIKE
                CONCAT('%', LOWER(:buscar), '%')
            """)
    List<Producto> catalogo(
            @Param("buscar") String buscar);

    // Filtrar productos activos por categoría y nombre
    @Query("""
            SELECT p FROM Producto p
            WHERE p.estado = true
            AND p.categoria.id = :categoria
            AND LOWER(p.nombre) LIKE
                CONCAT('%', LOWER(:buscar), '%')
            """)
    List<Producto> catalogoPorCategoria(
            @Param("categoria") Long categoria,
            @Param("buscar") String buscar);

    // Productos destacados para Home
    List<Producto> findByEstadoTrueAndDestacadoTrue();

    // Bloquear producto al realizar operaciones de compra
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> bloquear(@Param("id") Long id);
}
