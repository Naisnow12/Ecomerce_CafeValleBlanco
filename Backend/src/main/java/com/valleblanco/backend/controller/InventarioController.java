package com.valleblanco.backend.controller;

import com.valleblanco.backend.dto.MovimientoStockRequest;
import com.valleblanco.backend.model.MovimientoStock;
import com.valleblanco.backend.model.Product;
import com.valleblanco.backend.model.Usuario;
import com.valleblanco.backend.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_STOCK')")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(inventarioService.obtenerDashboardInventario());
    }

    @GetMapping("/movimientos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_STOCK')")
    public ResponseEntity<List<MovimientoStock>> getHistorialMovimientos() {
        return ResponseEntity.ok(inventarioService.obtenerHistorial());
    }

    @PostMapping("/movimientos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_STOCK')")
    public ResponseEntity<MovimientoStock> registrarMovimiento(
            @RequestBody MovimientoStockRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        String email = usuario != null ? usuario.getEmail() : "ANONIMO";
        String rol = usuario != null && !usuario.getRoles().isEmpty() ? usuario.getRoles().get(0).getNombre() : "UNKNOWN";
        return ResponseEntity.ok(inventarioService.registrarMovimiento(request, email, rol));
    }

    @GetMapping("/alertas")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_STOCK')")
    public ResponseEntity<List<Product>> getAlertas() {
        return ResponseEntity.ok(inventarioService.obtenerAlertasStockBajo());
    }

    @PutMapping("/productos/{id}/nivel-minimo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_STOCK')")
    public ResponseEntity<Product> actualizarNivelMinimo(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> payload,
            @AuthenticationPrincipal Usuario usuario
    ) {
        Integer nivelMinimo = payload.get("nivelMinimo");
        String email = usuario != null ? usuario.getEmail() : "ANONIMO";
        String rol = usuario != null && !usuario.getRoles().isEmpty() ? usuario.getRoles().get(0).getNombre() : "UNKNOWN";
        return ResponseEntity.ok(inventarioService.actualizarNivelMinimo(id, nivelMinimo, email, rol));
    }
}
