package com.valleblanco.backend.repository;

import com.valleblanco.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNombre(String nombre);
}
