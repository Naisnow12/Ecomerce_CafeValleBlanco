package pe.valleblanco.backend.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.valleblanco.backend.model.Usuario;

public interface UsuarioRepo extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
