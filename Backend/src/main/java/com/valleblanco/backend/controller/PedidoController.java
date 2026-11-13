package com.valleblanco.backend.controller;

import com.valleblanco.backend.dto.CrearPedidoRequest;
import com.valleblanco.backend.dto.PedidoResponse;
import com.valleblanco.backend.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponse> crearPedido(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CrearPedidoRequest request) {
        return ResponseEntity.ok(pedidoService.crearPedido(userDetails.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> getMisPedidos(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pedidoService.getMisPedidos(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> getDetallePedido(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pedidoService.getDetallePedido(userDetails.getUsername(), id));
    }
}
