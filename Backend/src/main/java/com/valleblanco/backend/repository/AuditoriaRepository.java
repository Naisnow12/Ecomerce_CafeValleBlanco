package com.valleblanco.backend.repository;

import com.valleblanco.backend.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findAllByOrderByCreadoEnDesc();
    List<Auditoria> findByEmailUsuarioOrderByCreadoEnDesc(String emailUsuario);
    List<Auditoria> findByModuloOrderByCreadoEnDesc(String modulo);
}
