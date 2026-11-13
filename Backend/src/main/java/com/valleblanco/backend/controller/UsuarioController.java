package com.valleblanco.backend.controller;

import com.valleblanco.backend.dto.*;
import com.valleblanco.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioProfileResponse> getPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(usuarioService.getProfile(userDetails.getUsername()));
    }

    @PutMapping("/perfil")
    public ResponseEntity<UsuarioProfileResponse> updatePerfil(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(usuarioService.updateProfile(userDetails.getUsername(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        usuarioService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/direcciones")
    public ResponseEntity<List<DireccionResponse>> getDirecciones(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(usuarioService.getDirecciones(userDetails.getUsername()));
    }

    @PostMapping("/direcciones")
    public ResponseEntity<DireccionResponse> addDireccion(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DireccionRequest request) {
        return ResponseEntity.ok(usuarioService.addDireccion(userDetails.getUsername(), request));
    }

    @PutMapping("/direcciones/{id}")
    public ResponseEntity<DireccionResponse> updateDireccion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DireccionRequest request) {
        return ResponseEntity.ok(usuarioService.updateDireccion(id, userDetails.getUsername(), request));
    }

    @DeleteMapping("/direcciones/{id}")
    public ResponseEntity<Void> deleteDireccion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        usuarioService.deleteDireccion(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
