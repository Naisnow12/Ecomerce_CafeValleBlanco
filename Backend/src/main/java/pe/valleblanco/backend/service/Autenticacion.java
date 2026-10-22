package pe.valleblanco.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pe.valleblanco.backend.model.*;
import pe.valleblanco.backend.repo.*;
import pe.valleblanco.backend.dto.Requests;
import java.time.*;
import java.security.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

@Service
public class Autenticacion {
    private final UsuarioRepo usuarios;
    private final SesionRepo sesiones;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    public Autenticacion(UsuarioRepo u, SesionRepo s, PasswordEncoder e) {
        usuarios = u;
        sesiones = s;
        encoder = e;
    }

    public Object registro(Requests.Registro r) {
        String email = r.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Correo registrado");
        Usuario u = new Usuario();
        u.nombre = r.nombre().trim();
        u.email = email;
        u.passwordHash = encoder.encode(r.password());
        usuarios.save(u);
        return Map.of("id", u.id, "nombre", u.nombre, "email", u.email);
    }

    public Object login(Requests.Login r) {
        Usuario u = usuarios.findByEmail(r.email().trim().toLowerCase(Locale.ROOT)).orElse(null);
        if (u == null || !u.activo || !encoder.matches(r.password(), u.passwordHash))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        byte[] b = new byte[32];
        random.nextBytes(b);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
        Sesion sesion = new Sesion();
        sesion.tokenHash = hash(token);
        sesion.usuario = u;
        sesion.expiraEn = Instant.now().plus(Duration.ofDays(1));
        sesiones.save(sesion);
        return Map.of("token", token, "tipo", "Bearer", "expiraEn", sesion.expiraEn, "usuario",
                Map.of("id", u.id, "nombre", u.nombre, "email", u.email, "rol", u.rol));
    }

    public Usuario usuario(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token requerido");
        String token = authorization.substring(7);
        Sesion sesion = sesiones.findById(hash(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido"));
        if (sesion.expiraEn.isBefore(Instant.now()) || !sesion.usuario.activo)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión vencida");
        return sesion.usuario;
    }

    public Usuario admin(String header) {
        Usuario u = usuario(header);
        if (!"ADMIN".equals(u.rol))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores");
        return u;
    }

    public void logout(String header) {
        usuario(header);
        sesiones.deleteById(hash(header.substring(7)));
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
