package com.valleblanco.backend.controller;

import com.valleblanco.backend.dto.PagoResponse;
import com.valleblanco.backend.dto.PedidoResponse;
import com.valleblanco.backend.model.Pedido;
import com.valleblanco.backend.service.PagoService;
import com.valleblanco.backend.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;
    private final PedidoService pedidoService;

    @PostMapping("/iniciar/{pedidoId}")
    public ResponseEntity<PagoResponse> iniciarPago(
            @PathVariable Long pedidoId,
            Authentication authentication
    ) {
        PagoResponse response = pagoService.iniciarPago(pedidoId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Webhook / Notificación del proveedor de pagos para actualizar el estado del pedido.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> webhookNotificacion(
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        // En un webhook real, se verifica la firma de cabecera y el id de transacción
        return ResponseEntity.ok(Map.of("status", "received"));
    }

    /**
     * Endpoint para confirmar/actualizar estado del pedido tras retorno del Sandbox.
     */
    @PatchMapping("/estado/{pedidoId}")
    public ResponseEntity<PedidoResponse> actualizarEstado(
            @PathVariable Long pedidoId,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String nuevoEstado = body.getOrDefault("estado", "PENDIENTE");
        Pedido pedido = pagoService.actualizarEstadoPago(pedidoId, nuevoEstado);
        return ResponseEntity.ok(pedidoService.getDetallePedido(authentication.getName(), pedido.getId()));
    }
}
