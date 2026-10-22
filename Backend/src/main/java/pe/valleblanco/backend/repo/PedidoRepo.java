package pe.valleblanco.backend.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.valleblanco.backend.model.Pedido;

public interface PedidoRepo extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioIdOrderByCreadoEnDesc(Long id);
}
