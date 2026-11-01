package com.valleblanco.backend.service;

import com.valleblanco.backend.dto.*;
import com.valleblanco.backend.model.Direccion;
import com.valleblanco.backend.model.Usuario;
import com.valleblanco.backend.repository.DireccionRepository;
import com.valleblanco.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final DireccionRepository direccionRepository;
    private final PasswordEncoder passwordEncoder;

    private Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + email));
    }

    public UsuarioProfileResponse getProfile(String email) {
        Usuario u = findByEmail(email);
        return toProfileResponse(u);
    }

    @Transactional
    public UsuarioProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        Usuario u = findByEmail(email);
        u.setNombres(request.getNombres());
        u.setApellidos(request.getApellidos());
        u.setTelefono(request.getTelefono());
        u.setActualizadoEn(LocalDateTime.now());
        usuarioRepository.save(u);
        return toProfileResponse(u);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        Usuario u = findByEmail(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), u.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Las nuevas contraseñas no coinciden");
        }

        u.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        u.setActualizadoEn(LocalDateTime.now());
        usuarioRepository.save(u);
    }

    public List<DireccionResponse> getDirecciones(String email) {
        Usuario u = findByEmail(email);
        return direccionRepository.findByUsuarioId(u.getId())
                .stream()
                .map(this::toDireccionResponse)
                .toList();
    }

    @Transactional
    public DireccionResponse addDireccion(String email, DireccionRequest request) {
        Usuario u = findByEmail(email);
        Direccion dir = buildDireccion(request, u);
        Direccion saved = direccionRepository.save(dir);
        return toDireccionResponse(saved);
    }

    @Transactional
    public DireccionResponse updateDireccion(Long id, String email, DireccionRequest request) {
        Usuario u = findByEmail(email);
        Direccion dir = direccionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dirección no encontrada: " + id));

        if (!dir.getUsuario().getId().equals(u.getId())) {
            throw new IllegalArgumentException("La dirección no pertenece al usuario");
        }

        dir.setAlias(request.getAlias());
        dir.setDestinatario(request.getDestinatario());
        dir.setLinea1(request.getLinea1());
        dir.setLinea2(request.getLinea2());
        dir.setDistrito(request.getDistrito());
        dir.setProvincia(request.getProvincia());
        dir.setDepartamento(request.getDepartamento());
        dir.setCodigoPostal(request.getCodigoPostal());
        dir.setTelefono(request.getTelefono());
        dir.setEsPrincipal(Boolean.TRUE.equals(request.getEsPrincipal()));

        return toDireccionResponse(direccionRepository.save(dir));
    }

    @Transactional
    public void deleteDireccion(Long id, String email) {
        Usuario u = findByEmail(email);
        Direccion dir = direccionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dirección no encontrada: " + id));

        if (!dir.getUsuario().getId().equals(u.getId())) {
            throw new IllegalArgumentException("La dirección no pertenece al usuario");
        }

        direccionRepository.delete(dir);
    }

    // --- Helpers ---

    private Direccion buildDireccion(DireccionRequest req, Usuario usuario) {
        return Direccion.builder()
                .usuario(usuario)
                .alias(req.getAlias())
                .destinatario(req.getDestinatario())
                .linea1(req.getLinea1())
                .linea2(req.getLinea2())
                .distrito(req.getDistrito())
                .provincia(req.getProvincia())
                .departamento(req.getDepartamento())
                .codigoPostal(req.getCodigoPostal())
                .telefono(req.getTelefono())
                .esPrincipal(Boolean.TRUE.equals(req.getEsPrincipal()))
                .build();
    }

    private UsuarioProfileResponse toProfileResponse(Usuario u) {
        return UsuarioProfileResponse.builder()
                .id(u.getId())
                .nombres(u.getNombres())
                .apellidos(u.getApellidos())
                .email(u.getEmail())
                .telefono(u.getTelefono())
                .build();
    }

    private DireccionResponse toDireccionResponse(Direccion d) {
        return DireccionResponse.builder()
                .id(d.getId())
                .alias(d.getAlias())
                .destinatario(d.getDestinatario())
                .linea1(d.getLinea1())
                .linea2(d.getLinea2())
                .distrito(d.getDistrito())
                .provincia(d.getProvincia())
                .departamento(d.getDepartamento())
                .codigoPostal(d.getCodigoPostal())
                .telefono(d.getTelefono())
                .esPrincipal(d.getEsPrincipal())
                .build();
    }
}
