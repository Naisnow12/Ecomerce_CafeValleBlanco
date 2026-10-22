package com.valleblanco.backend.repository;

import com.valleblanco.backend.model.NotaCata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotaCataRepository extends JpaRepository<NotaCata, Long> {
    Optional<NotaCata> findByNombre(String nombre);
}
