package pe.valleblanco.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.valleblanco.backend.model.Categoria;

public interface CategoriaRepo extends JpaRepository<Categoria, Long> {
}
