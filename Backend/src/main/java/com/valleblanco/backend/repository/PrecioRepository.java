package com.valleblanco.backend.repository;

import com.valleblanco.backend.model.Precio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrecioRepository extends JpaRepository<Precio, Long> {
}
