package com.valleblanco.backend.controller;

import com.valleblanco.backend.dto.PedidoResponse;
import com.valleblanco.backend.dto.ReporteMetricasResponse;
import com.valleblanco.backend.model.Usuario;
import com.valleblanco.backend.service.AdminService;
import com.valleblanco.backend.service.ReportePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ReportePdfService reportePdfService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<Map<String, Object>>> listarUsuarios() {
        return ResponseEntity.ok(adminService.listarUsuarios());
    }

    @PutMapping("/usuarios/{id}/estado")
    public ResponseEntity<Void> cambiarEstadoUsuario(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload,
            @AuthenticationPrincipal Usuario admin
    ) {
        boolean activo = Boolean.TRUE.equals(payload.get("activo"));
        adminService.cambiarEstadoUsuario(id, activo, admin.getEmail());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/usuarios/{id}/roles")
    public ResponseEntity<Void> asignarRoles(
            @PathVariable Long id,
            @RequestBody Map<String, List<String>> payload,
            @AuthenticationPrincipal Usuario admin
    ) {
        List<String> roles = payload.get("roles");
        adminService.asignarRoles(id, roles, admin.getEmail());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pedidos")
    public ResponseEntity<List<PedidoResponse>> listarPedidos() {
        return ResponseEntity.ok(adminService.listarTodosLosPedidos());
    }

    @PatchMapping("/pedidos/{id}/estado")
    public ResponseEntity<PedidoResponse> actualizarEstadoPedido(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal Usuario admin
    ) {
        String nuevoEstado = payload.get("estado");
        return ResponseEntity.ok(adminService.actualizarEstadoPedido(id, nuevoEstado, admin.getEmail()));
    }

    @GetMapping("/reportes")
    public ResponseEntity<ReporteMetricasResponse> getReportes(
            @RequestParam(defaultValue = "HISTORICO") String periodo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin
    ) {
        return ResponseEntity.ok(adminService.generarReporteMetricas(periodo, estado, metodoPago, fechaInicio, fechaFin));
    }

    @GetMapping("/reportes/pdf")
    public ResponseEntity<byte[]> descargarReportePdf(
            @RequestParam(defaultValue = "HISTORICO") String periodo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @AuthenticationPrincipal Usuario admin
    ) {
        ReporteMetricasResponse metricas = adminService.generarReporteMetricas(periodo, estado, metodoPago, fechaInicio, fechaFin);
        byte[] pdfBytes = reportePdfService.generarReportePdf(metricas, admin != null ? admin.getEmail() : "Administrador");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "Reporte_Metricas_ValleBlanco_" + LocalDate.now() + ".pdf";
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
