package com.valleblanco.backend.controller;

import com.valleblanco.backend.dto.CrearPedidoRequest;
import com.valleblanco.backend.dto.PedidoResponse;
import com.valleblanco.backend.service.ComprobantePdfService;
import com.valleblanco.backend.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final ComprobantePdfService comprobantePdfService;

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

    @GetMapping("/{id}/comprobante")
    public ResponseEntity<byte[]> descargarComprobante(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PedidoResponse pedido = pedidoService.getDetallePedido(userDetails.getUsername(), id);
        byte[] pdfBytes = comprobantePdfService.generarComprobante(pedido);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "Comprobante_" + (pedido.getNumeroPedido() != null ? pedido.getNumeroPedido() : id) + ".pdf";
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
