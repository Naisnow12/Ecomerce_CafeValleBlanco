package pe.valleblanco.backend.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import pe.valleblanco.backend.dto.Requests;
import pe.valleblanco.backend.service.Autenticacion;
import pe.valleblanco.backend.model.Usuario;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final Autenticacion auth;

    public AuthController(Autenticacion a) {
        auth = a;
    }

    @PostMapping("/registro")
    public Object registro(@Valid @RequestBody Requests.Registro r) {
        return auth.registro(r);
    }

    @PostMapping("/login")
    public Object login(@Valid @RequestBody Requests.Login r) {
        return auth.login(r);
    }

    @GetMapping("/me")
    public Object me(@RequestHeader(value = "Authorization", required = false) String h) {
        Usuario u = auth.usuario(h);
        return Map.of("id", u.id, "nombre", u.nombre, "email", u.email, "rol", u.rol);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(value = "Authorization", required = false) String h) {
        auth.logout(h);
    }
}
