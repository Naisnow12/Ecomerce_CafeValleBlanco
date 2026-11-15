package com.valleblanco.backend.repository;

import com.valleblanco.backend.model.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findAllByOrderByCreadoEnDesc();
    List<MovimientoStock> findByProductoIdOrderByCreadoEnDesc(Long productoId);
    List<MovimientoStock> findTop20ByOrderByCreadoEnDesc();
}
