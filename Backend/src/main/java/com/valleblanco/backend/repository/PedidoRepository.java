package com.valleblanco.backend.repository;

import com.valleblanco.backend.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);
    Optional<Pedido> findByIdAndUsuarioId(Long id, Long usuarioId);
}
