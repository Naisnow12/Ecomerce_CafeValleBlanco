package com.valleblanco.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {
    private String proveedor;          // "MERCADO_PAGO" o "CULQI"
    private String estadoPago;         // "PENDIENTE", "PAGADO", "RECHAZADO"
    private String idTransaccion;      // ID asignado por la pasarela
    private String initPointUrl;       // URL para redirigir al checkout de la pasarela
    private BigDecimal monto;
    private String moneda;
    private String mensaje;
}
