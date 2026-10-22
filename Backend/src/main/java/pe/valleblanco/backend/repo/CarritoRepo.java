package pe.valleblanco.backend.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.valleblanco.backend.model.CarritoItem;

public interface CarritoRepo extends JpaRepository<CarritoItem, Long> {
    List<CarritoItem> findByUsuarioIdOrderById(Long id);

    Optional<CarritoItem> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    void deleteByUsuarioId(Long usuarioId);
}
