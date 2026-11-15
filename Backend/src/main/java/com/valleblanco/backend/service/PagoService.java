package com.valleblanco.backend.service;

import com.valleblanco.backend.dto.PagoResponse;
import com.valleblanco.backend.model.Pedido;
import com.valleblanco.backend.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoService {

    private final PedidoRepository pedidoRepository;

    @Value("${app.payments.mercadopago.access-token:TEST-TOKEN-PENDIENTE}")
    private String mercadoPagoAccessToken;

    @Value("${app.payments.mercadopago.public-key:TEST-PUBLIC-KEY-PENDIENTE}")
    private String mercadoPagoPublicKey;

    @Value("${app.payments.sandbox:true}")
    private boolean sandboxMode;

    /**
     * Inicia el proceso de checkout en Sandbox para un pedido determinado.
     * Retorna los datos necesarios y la URL de redirección a la pasarela.
     */
    @Transactional
    public PagoResponse iniciarPago(Long pedidoId, String emailUsuario) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado: " + pedidoId));

        if (!pedido.getUsuario().getEmail().equalsIgnoreCase(emailUsuario)) {
            throw new IllegalArgumentException("No tienes permiso para gestionar este pedido.");
        }

        String transactionId = "TX-MP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // En entorno Sandbox oficial de Mercado Pago / Culqi
        // Si no se han suministrado credenciales de producción, se genera la referencia segura
        String checkoutUrl = "https://sandbox.mercadopago.com.pe/checkout/v1/redirect?pref_id=" + transactionId;

        log.info("Iniciando solicitud de pago Sandbox para pedido {} por un monto de S/ {}", pedido.getNumeroPedido(), pedido.getTotal());

        return PagoResponse.builder()
                .proveedor("MERCADO_PAGO")
                .estadoPago("PENDIENTE")
                .idTransaccion(transactionId)
                .initPointUrl(checkoutUrl)
                .monto(pedido.getTotal())
                .moneda("PEN")
                .mensaje("Sesión de pago Sandbox iniciada para el pedido " + pedido.getNumeroPedido())
                .build();
    }

    /**
     * Procesa la confirmación o webhook del proveedor de pagos y actualiza el estado del pedido.
     * Estados admitidos: PENDIENTE, PAGADO, RECHAZADO, CANCELADO, EN_PREPARACION, ENVIADO, ENTREGADO.
     */
    @Transactional
    public Pedido actualizarEstadoPago(Long pedidoId, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado: " + pedidoId));

        String estadoNormalizado = nuevoEstado.trim().toUpperCase();
        switch (estadoNormalizado) {
            case "PAGADO":
            case "PAGO_RECHAZADO":
            case "CANCELADO":
            case "EN_PREPARACION":
            case "ENVIADO":
            case "ENTREGADO":
            case "PENDIENTE":
                pedido.setEstado(estadoNormalizado);
                break;
            default:
                throw new IllegalArgumentException("Estado de pedido no válido: " + nuevoEstado);
        }

        pedido.setActualizadoEn(LocalDateTime.now());
        log.info("Estado del pedido {} actualizado a {}", pedido.getNumeroPedido(), pedido.getEstado());
        return pedidoRepository.save(pedido);
    }
}
