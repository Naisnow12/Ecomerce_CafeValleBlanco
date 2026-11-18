package com.valleblanco.backend.controller;

import com.valleblanco.backend.dto.ActualizarCantidadRequest;
import com.valleblanco.backend.dto.AgregarAlCarritoRequest;
import com.valleblanco.backend.dto.CarritoResponse;
import com.valleblanco.backend.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    public ResponseEntity<CarritoResponse> getCarrito(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(carritoService.getCarrito(userDetails.getUsername()));
    }

    @PostMapping("/items")
    public ResponseEntity<CarritoResponse> agregarItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AgregarAlCarritoRequest request) {
        return ResponseEntity.ok(carritoService.agregarItem(userDetails.getUsername(), request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CarritoResponse> actualizarCantidad(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ActualizarCantidadRequest request) {
        return ResponseEntity.ok(carritoService.actualizarCantidad(userDetails.getUsername(), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CarritoResponse> eliminarItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(carritoService.eliminarItem(userDetails.getUsername(), itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> vaciarCarrito(@AuthenticationPrincipal UserDetails userDetails) {
        carritoService.vaciarCarrito(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
