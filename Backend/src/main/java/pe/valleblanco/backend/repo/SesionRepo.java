package pe.valleblanco.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.valleblanco.backend.model.Sesion;

public interface SesionRepo extends JpaRepository<Sesion, String> {
}
