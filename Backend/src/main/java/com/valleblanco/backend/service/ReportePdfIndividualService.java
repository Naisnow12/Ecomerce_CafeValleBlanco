package com.valleblanco.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.valleblanco.backend.dto.ReporteMetricasResponse;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ReportePdfIndividualService {

    private static final Color COLOR_PRIMARY    = new Color(27, 67, 50);
    private static final Color COLOR_SECONDARY  = new Color(184, 115, 51);
    private static final Color COLOR_BG_HEADER  = new Color(245, 245, 240);
    private static final Color COLOR_ROW_ALT    = new Color(250, 250, 247);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 100, 100);
    private static final Color COLOR_BORDER     = new Color(220, 220, 215);

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────────────────────────────────────────────────────
    // 1. REPORTE DE VENTAS
    // ─────────────────────────────────────────────────────────────────────────
    public byte[] generarReporteVentas(ReporteMetricasResponse metricas, String adminEmail) {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());
            document.open();

            agregarCabecera(document, metricas, adminEmail, "REPORTE DE VENTAS E INGRESOS");

            // KPIs
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);
            Paragraph kpiTitle = new Paragraph("1. INDICADORES CLAVE", sectionFont);
            kpiTitle.setSpacingAfter(8);
            document.add(kpiTitle);

            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            kpiTable.setWidths(new float[]{25f, 25f, 25f, 25f});
            kpiTable.setSpacingAfter(15);

            String ingresos = "S/ " + (metricas.getTotalIngresos() != null ? moneyFormat.format(metricas.getTotalIngresos()) : "0.00");
            String ticket   = "S/ " + (metricas.getTicketPromedio() != null ? moneyFormat.format(metricas.getTicketPromedio()) : "0.00");
            String completados = String.valueOf(metricas.getPedidosCompletados());
            String unidades    = String.valueOf(metricas.getTotalUnidadesVendidas());

            kpiTable.addCell(crearKpiCell("INGRESOS TOTALES",   ingresos,    "Pedidos válidos no cancelados"));
            kpiTable.addCell(crearKpiCell("TICKET PROMEDIO",    ticket,      "Venta promedio por pedido"));
            kpiTable.addCell(crearKpiCell("PEDIDOS COMPLETADOS", completados, "Estado: COMPLETADO"));
            kpiTable.addCell(crearKpiCell("UNIDADES VENDIDAS",   unidades,    "Bolsas de café despachadas"));
            document.add(kpiTable);

            // Tabla: Desglose por Método de Pago
            Paragraph mpTitle = new Paragraph("2. DESGLOSE POR MÉTODO DE PAGO", sectionFont);
            mpTitle.setSpacingAfter(8);
            document.add(mpTitle);

            PdfPTable mpTable = new PdfPTable(3);
            mpTable.setWidthPercentage(100);
            mpTable.setWidths(new float[]{50f, 30f, 20f});
            mpTable.setSpacingAfter(15);
            mpTable.addCell(crearHeaderCell("Método de Pago", Element.ALIGN_LEFT));
            mpTable.addCell(crearHeaderCell("Monto (S/)", Element.ALIGN_RIGHT));
            mpTable.addCell(crearHeaderCell("% del Total", Element.ALIGN_RIGHT));

            if (metricas.getVentasPorMetodoPago() != null && !metricas.getVentasPorMetodoPago().isEmpty()) {
                BigDecimal totalGeneral = metricas.getTotalIngresos() != null ? metricas.getTotalIngresos() : BigDecimal.ONE;
                boolean alt = false;
                for (Map.Entry<String, BigDecimal> entry : metricas.getVentasPorMetodoPago().entrySet()) {
                    Color c = alt ? COLOR_ROW_ALT : Color.WHITE;
                    double pct = totalGeneral.compareTo(BigDecimal.ZERO) != 0
                            ? entry.getValue().doubleValue() / totalGeneral.doubleValue() * 100.0 : 0.0;
                    mpTable.addCell(crearDataCell(entry.getKey(), c, Element.ALIGN_LEFT, false));
                    mpTable.addCell(crearDataCell("S/ " + moneyFormat.format(entry.getValue()), c, Element.ALIGN_RIGHT, false));
                    mpTable.addCell(crearDataCell(String.format("%.1f%%", pct), c, Element.ALIGN_RIGHT, false));
                    alt = !alt;
                }
            } else {
                mpTable.addCell(crearDataCell("Sin datos", Color.WHITE, Element.ALIGN_LEFT, false));
                mpTable.addCell(crearDataCell("S/ 0.00", Color.WHITE, Element.ALIGN_RIGHT, false));
                mpTable.addCell(crearDataCell("0.0%", Color.WHITE, Element.ALIGN_RIGHT, false));
            }
            document.add(mpTable);

            // Tabla: Desglose por Estado de Pedidos
            Paragraph estTitle = new Paragraph("3. DESGLOSE POR ESTADO DE PEDIDOS", sectionFont);
            estTitle.setSpacingAfter(8);
            document.add(estTitle);

            PdfPTable estTable = new PdfPTable(3);
            estTable.setWidthPercentage(100);
            estTable.setWidths(new float[]{50f, 30f, 20f});
            estTable.setSpacingAfter(15);
            estTable.addCell(crearHeaderCell("Estado", Element.ALIGN_LEFT));
            estTable.addCell(crearHeaderCell("Cantidad", Element.ALIGN_RIGHT));
            estTable.addCell(crearHeaderCell("% del Total", Element.ALIGN_RIGHT));

            if (metricas.getPedidosPorEstado() != null && !metricas.getPedidosPorEstado().isEmpty()) {
                long totalPed = metricas.getTotalPedidos();
                boolean alt = false;
                for (Map.Entry<String, Long> entry : metricas.getPedidosPorEstado().entrySet()) {
                    Color c = alt ? COLOR_ROW_ALT : Color.WHITE;
                    double pct = totalPed > 0 ? entry.getValue().doubleValue() / totalPed * 100.0 : 0.0;
                    estTable.addCell(crearDataCell(entry.getKey(), c, Element.ALIGN_LEFT, false));
                    estTable.addCell(crearDataCell(String.valueOf(entry.getValue()), c, Element.ALIGN_RIGHT, false));
                    estTable.addCell(crearDataCell(String.format("%.1f%%", pct), c, Element.ALIGN_RIGHT, false));
                    alt = !alt;
                }
            } else {
                estTable.addCell(crearDataCell("Sin pedidos", Color.WHITE, Element.ALIGN_LEFT, false));
                estTable.addCell(crearDataCell("0", Color.WHITE, Element.ALIGN_RIGHT, false));
                estTable.addCell(crearDataCell("0.0%", Color.WHITE, Element.ALIGN_RIGHT, false));
            }
            document.add(estTable);

            // Tabla: Ventas por Departamento
            if (metricas.getVentasPorDepartamento() != null && !metricas.getVentasPorDepartamento().isEmpty()) {
                Paragraph depTitle = new Paragraph("4. VENTAS POR DEPARTAMENTO", sectionFont);
                depTitle.setSpacingAfter(8);
                document.add(depTitle);

                PdfPTable depTable = new PdfPTable(5);
                depTable.setWidthPercentage(100);
                depTable.setWidths(new float[]{38f, 15f, 15f, 20f, 12f});
                depTable.setSpacingAfter(15);
                depTable.addCell(crearHeaderCell("Departamento", Element.ALIGN_LEFT));
                depTable.addCell(crearHeaderCell("Pedidos", Element.ALIGN_RIGHT));
                depTable.addCell(crearHeaderCell("Unidades", Element.ALIGN_RIGHT));
                depTable.addCell(crearHeaderCell("Total (S/)", Element.ALIGN_RIGHT));
                depTable.addCell(crearHeaderCell("Part. %", Element.ALIGN_RIGHT));

                boolean altDep = false;
                for (ReporteMetricasResponse.VentaDepartamentoReporte dep : metricas.getVentasPorDepartamento()) {
                    Color c = altDep ? COLOR_ROW_ALT : Color.WHITE;
                    depTable.addCell(crearDataCell(dep.getDepartamento(), c, Element.ALIGN_LEFT, true));
                    depTable.addCell(crearDataCell(String.valueOf(dep.getCantidadPedidos()), c, Element.ALIGN_RIGHT, false));
                    depTable.addCell(crearDataCell(String.valueOf(dep.getUnidadesVendidas()), c, Element.ALIGN_RIGHT, false));
                    depTable.addCell(crearDataCell("S/ " + moneyFormat.format(dep.getTotalGenerado()), c, Element.ALIGN_RIGHT, false));
                    depTable.addCell(crearDataCell(String.format("%.1f%%", dep.getPorcentaje()), c, Element.ALIGN_RIGHT, false));
                    altDep = !altDep;
                }
                document.add(depTable);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte de ventas: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. REPORTE DE PRODUCTOS
    // ─────────────────────────────────────────────────────────────────────────
    public byte[] generarReporteProductos(ReporteMetricasResponse metricas, String adminEmail) {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());
            document.open();

            agregarCabecera(document, metricas, adminEmail, "REPORTE DE PRODUCTOS Y CATÁLOGO");

            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);

            // Tabla: Top 8 productos más vendidos
            Paragraph topTitle = new Paragraph("1. TOP 8 PRODUCTOS MÁS VENDIDOS", sectionFont);
            topTitle.setSpacingAfter(8);
            document.add(topTitle);

            PdfPTable topTable = new PdfPTable(5);
            topTable.setWidthPercentage(100);
            topTable.setWidths(new float[]{8f, 48f, 16f, 16f, 12f});
            topTable.setSpacingAfter(15);
            topTable.addCell(crearHeaderCell("#", Element.ALIGN_CENTER));
            topTable.addCell(crearHeaderCell("Producto", Element.ALIGN_LEFT));
            topTable.addCell(crearHeaderCell("Unidades", Element.ALIGN_RIGHT));
            topTable.addCell(crearHeaderCell("Total (S/)", Element.ALIGN_RIGHT));
            topTable.addCell(crearHeaderCell("Part. %", Element.ALIGN_RIGHT));

            if (metricas.getTopProductos() != null && !metricas.getTopProductos().isEmpty()) {
                int rank = 1;
                for (ReporteMetricasResponse.TopProductoReporte prod : metricas.getTopProductos()) {
                    if (rank > 8) break;
                    Color rowColor = (rank % 2 == 0) ? COLOR_ROW_ALT : Color.WHITE;
                    topTable.addCell(crearDataCell(String.valueOf(rank), rowColor, Element.ALIGN_CENTER, true));
                    topTable.addCell(crearDataCell(prod.getNombre(), rowColor, Element.ALIGN_LEFT, false));
                    topTable.addCell(crearDataCell(String.valueOf(prod.getUnidadesVendidas()), rowColor, Element.ALIGN_RIGHT, false));
                    topTable.addCell(crearDataCell("S/ " + moneyFormat.format(prod.getTotalGenerado()), rowColor, Element.ALIGN_RIGHT, false));
                    topTable.addCell(crearDataCell(String.format("%.1f%%", prod.getPorcentaje()), rowColor, Element.ALIGN_RIGHT, false));
                    rank++;
                }
            } else {
                topTable.addCell(crearDataCell("Sin datos de productos", Color.WHITE, Element.ALIGN_CENTER, false));
                PdfPCell empty = new PdfPCell(new Phrase(""));
                empty.setColspan(4);
                empty.setBorderColor(COLOR_BORDER);
                topTable.addCell(empty);
            }
            document.add(topTable);

            // Tabla: Alertas de Stock
            Paragraph stockTitle = new Paragraph("2. ALERTAS DE INVENTARIO / STOCK CRÍTICO", sectionFont);
            stockTitle.setSpacingAfter(8);
            document.add(stockTitle);

            PdfPTable stockTable = new PdfPTable(4);
            stockTable.setWidthPercentage(100);
            stockTable.setWidths(new float[]{50f, 18f, 18f, 14f});
            stockTable.setSpacingAfter(15);
            stockTable.addCell(crearHeaderCell("Producto", Element.ALIGN_LEFT));
            stockTable.addCell(crearHeaderCell("Stock Actual", Element.ALIGN_RIGHT));
            stockTable.addCell(crearHeaderCell("Nivel Mínimo", Element.ALIGN_RIGHT));
            stockTable.addCell(crearHeaderCell("Estado", Element.ALIGN_CENTER));

            if (metricas.getStockCritico() != null && !metricas.getStockCritico().isEmpty()) {
                boolean alt = false;
                for (ReporteMetricasResponse.StockCriticoReporte sc : metricas.getStockCritico()) {
                    Color rowColor = alt ? COLOR_ROW_ALT : Color.WHITE;
                    stockTable.addCell(crearDataCell(sc.getNombre(), rowColor, Element.ALIGN_LEFT, false));
                    stockTable.addCell(crearDataCell(String.valueOf(sc.getStockActual()), rowColor, Element.ALIGN_RIGHT, false));
                    stockTable.addCell(crearDataCell(String.valueOf(sc.getNivelMinimo()), rowColor, Element.ALIGN_RIGHT, false));
                    stockTable.addCell(crearDataCell(sc.getEstado(), rowColor, Element.ALIGN_CENTER, true));
                    alt = !alt;
                }
            } else {
                stockTable.addCell(crearDataCell("Sin alertas de inventario", Color.WHITE, Element.ALIGN_LEFT, false));
                stockTable.addCell(crearDataCell("-", Color.WHITE, Element.ALIGN_RIGHT, false));
                stockTable.addCell(crearDataCell("-", Color.WHITE, Element.ALIGN_RIGHT, false));
                stockTable.addCell(crearDataCell("-", Color.WHITE, Element.ALIGN_CENTER, false));
            }
            document.add(stockTable);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte de productos: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. REPORTE DE PEDIDOS
    // ─────────────────────────────────────────────────────────────────────────
    public byte[] generarReportePedidos(ReporteMetricasResponse metricas, String adminEmail) {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());
            document.open();

            agregarCabecera(document, metricas, adminEmail, "REPORTE DE PEDIDOS");

            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);

            // KPIs
            Paragraph kpiTitle = new Paragraph("1. INDICADORES DE PEDIDOS", sectionFont);
            kpiTitle.setSpacingAfter(8);
            document.add(kpiTitle);

            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            kpiTable.setWidths(new float[]{25f, 25f, 25f, 25f});
            kpiTable.setSpacingAfter(15);
            kpiTable.addCell(crearKpiCell("TOTAL PEDIDOS",    String.valueOf(metricas.getTotalPedidos()),      "Todos los estados"));
            kpiTable.addCell(crearKpiCell("COMPLETADOS",      String.valueOf(metricas.getPedidosCompletados()), "Entregados al cliente"));
            kpiTable.addCell(crearKpiCell("PENDIENTES",       String.valueOf(metricas.getPedidosPendientes()),  "En proceso / por confirmar"));
            kpiTable.addCell(crearKpiCell("CANCELADOS",       String.valueOf(metricas.getPedidosCancelados()),  "Anulados en el período"));
            document.add(kpiTable);

            // Tabla completa de pedidos (sin límite)
            Paragraph pedidosTitle = new Paragraph("2. DETALLE DE PEDIDOS DEL PERÍODO", sectionFont);
            pedidosTitle.setSpacingAfter(8);
            document.add(pedidosTitle);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{18f, 20f, 26f, 14f, 12f, 10f});
            table.setSpacingAfter(10);
            table.addCell(crearHeaderCell("N° Pedido", Element.ALIGN_LEFT));
            table.addCell(crearHeaderCell("Fecha", Element.ALIGN_LEFT));
            table.addCell(crearHeaderCell("Cliente", Element.ALIGN_LEFT));
            table.addCell(crearHeaderCell("Método", Element.ALIGN_LEFT));
            table.addCell(crearHeaderCell("Total", Element.ALIGN_RIGHT));
            table.addCell(crearHeaderCell("Estado", Element.ALIGN_CENTER));

            if (metricas.getPedidos() != null && !metricas.getPedidos().isEmpty()) {
                boolean alt = false;
                for (ReporteMetricasResponse.PedidoResumenReporte ped : metricas.getPedidos()) {
                    Color rowColor = alt ? COLOR_ROW_ALT : Color.WHITE;
                    table.addCell(crearDataCell(ped.getNumeroPedido(), rowColor, Element.ALIGN_LEFT, true));
                    table.addCell(crearDataCell(ped.getFecha() != null ? ped.getFecha().format(dateFormat) : "-", rowColor, Element.ALIGN_LEFT, false));
                    table.addCell(crearDataCell(ped.getCliente() != null ? ped.getCliente() : "Cliente", rowColor, Element.ALIGN_LEFT, false));
                    table.addCell(crearDataCell(ped.getMetodoPago() != null ? ped.getMetodoPago() : "-", rowColor, Element.ALIGN_LEFT, false));
                    table.addCell(crearDataCell("S/ " + moneyFormat.format(ped.getTotal()), rowColor, Element.ALIGN_RIGHT, false));
                    table.addCell(crearDataCell(ped.getEstado(), rowColor, Element.ALIGN_CENTER, false));
                    alt = !alt;
                }
            } else {
                PdfPCell emptyCell = new PdfPCell(new Phrase("Sin pedidos en el período seleccionado",
                        FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXT_MUTED)));
                emptyCell.setColspan(6);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                emptyCell.setPadding(8);
                emptyCell.setBorderColor(COLOR_BORDER);
                table.addCell(emptyCell);
            }
            document.add(table);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte de pedidos: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. REPORTE DE CLIENTES
    // ─────────────────────────────────────────────────────────────────────────
    public byte[] generarReporteClientes(ReporteMetricasResponse metricas, String adminEmail) {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());
            document.open();

            agregarCabecera(document, metricas, adminEmail, "REPORTE DE CLIENTES Y COMPRADORES");

            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);

            // KPIs
            Paragraph kpiTitle = new Paragraph("1. INDICADORES DE CLIENTES", sectionFont);
            kpiTitle.setSpacingAfter(8);
            document.add(kpiTitle);

            PdfPTable kpiTable = new PdfPTable(3);
            kpiTable.setWidthPercentage(100);
            kpiTable.setWidths(new float[]{33f, 33f, 34f});
            kpiTable.setSpacingAfter(15);
            String ticket = "S/ " + (metricas.getTicketPromedio() != null ? moneyFormat.format(metricas.getTicketPromedio()) : "0.00");
            kpiTable.addCell(crearKpiCell("CLIENTES COMPRADORES", String.valueOf(metricas.getTotalClientesCompradores()), "Clientes con al menos 1 pedido"));
            kpiTable.addCell(crearKpiCell("TOTAL PEDIDOS",        String.valueOf(metricas.getTotalPedidos()),             "Todos los estados"));
            kpiTable.addCell(crearKpiCell("TICKET PROMEDIO",      ticket,                                                "Venta promedio por pedido"));
            document.add(kpiTable);

            // Tabla: Resumen de pedidos agrupado por cliente
            Paragraph clientesTitle = new Paragraph("2. PEDIDOS POR CLIENTE", sectionFont);
            clientesTitle.setSpacingAfter(8);
            document.add(clientesTitle);

            PdfPTable clientesTable = new PdfPTable(3);
            clientesTable.setWidthPercentage(100);
            clientesTable.setWidths(new float[]{55f, 20f, 25f});
            clientesTable.setSpacingAfter(15);
            clientesTable.addCell(crearHeaderCell("Cliente", Element.ALIGN_LEFT));
            clientesTable.addCell(crearHeaderCell("N° Pedidos", Element.ALIGN_RIGHT));
            clientesTable.addCell(crearHeaderCell("Total Gastado (S/)", Element.ALIGN_RIGHT));

            if (metricas.getPedidos() != null && !metricas.getPedidos().isEmpty()) {
                // Agrupar por cliente
                Map<String, long[]> clienteMap = new LinkedHashMap<>();
                for (ReporteMetricasResponse.PedidoResumenReporte ped : metricas.getPedidos()) {
                    String cliente = ped.getCliente() != null ? ped.getCliente() : "Desconocido";
                    BigDecimal total = ped.getTotal() != null ? ped.getTotal() : BigDecimal.ZERO;
                    clienteMap.computeIfAbsent(cliente, k -> new long[]{0, 0});
                    clienteMap.get(cliente)[0]++;
                    // Store cents to avoid floating point; use Long for cents
                    clienteMap.get(cliente)[1] += total.multiply(BigDecimal.valueOf(100)).longValue();
                }
                boolean alt = false;
                for (Map.Entry<String, long[]> entry : clienteMap.entrySet()) {
                    Color c = alt ? COLOR_ROW_ALT : Color.WHITE;
                    long cantidad = entry.getValue()[0];
                    BigDecimal totalCliente = BigDecimal.valueOf(entry.getValue()[1]).divide(BigDecimal.valueOf(100));
                    clientesTable.addCell(crearDataCell(entry.getKey(), c, Element.ALIGN_LEFT, false));
                    clientesTable.addCell(crearDataCell(String.valueOf(cantidad), c, Element.ALIGN_RIGHT, false));
                    clientesTable.addCell(crearDataCell("S/ " + moneyFormat.format(totalCliente), c, Element.ALIGN_RIGHT, false));
                    alt = !alt;
                }
            } else {
                clientesTable.addCell(crearDataCell("Sin clientes con pedidos", Color.WHITE, Element.ALIGN_LEFT, false));
                clientesTable.addCell(crearDataCell("0", Color.WHITE, Element.ALIGN_RIGHT, false));
                clientesTable.addCell(crearDataCell("S/ 0.00", Color.WHITE, Element.ALIGN_RIGHT, false));
            }
            document.add(clientesTable);

            // Tabla: Distribución por método de pago
            Paragraph mpTitle = new Paragraph("3. DISTRIBUCIÓN POR MÉTODO DE PAGO", sectionFont);
            mpTitle.setSpacingAfter(8);
            document.add(mpTitle);

            PdfPTable mpTable = new PdfPTable(2);
            mpTable.setWidthPercentage(100);
            mpTable.setWidths(new float[]{60f, 40f});
            mpTable.setSpacingAfter(15);
            mpTable.addCell(crearHeaderCell("Método de Pago", Element.ALIGN_LEFT));
            mpTable.addCell(crearHeaderCell("Ventas (S/)", Element.ALIGN_RIGHT));

            if (metricas.getVentasPorMetodoPago() != null && !metricas.getVentasPorMetodoPago().isEmpty()) {
                boolean alt = false;
                for (Map.Entry<String, BigDecimal> entry : metricas.getVentasPorMetodoPago().entrySet()) {
                    Color c = alt ? COLOR_ROW_ALT : Color.WHITE;
                    mpTable.addCell(crearDataCell(entry.getKey(), c, Element.ALIGN_LEFT, false));
                    mpTable.addCell(crearDataCell("S/ " + moneyFormat.format(entry.getValue()), c, Element.ALIGN_RIGHT, false));
                    alt = !alt;
                }
            } else {
                mpTable.addCell(crearDataCell("Sin datos", Color.WHITE, Element.ALIGN_LEFT, false));
                mpTable.addCell(crearDataCell("S/ 0.00", Color.WHITE, Element.ALIGN_RIGHT, false));
            }
            document.add(mpTable);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte de clientes: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS COMPARTIDOS
    // ─────────────────────────────────────────────────────────────────────────
    private void agregarCabecera(Document doc, ReporteMetricasResponse metricas, String adminEmail, String reportTitle)
            throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{65f, 35f});
        headerTable.setSpacingAfter(15);

        // Columna izquierda: Marca y título
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPadding(0);

        Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARY);
        Paragraph brand = new Paragraph("CAFÉ VALLE BLANCO", brandFont);
        brand.setSpacingAfter(2);
        leftCell.addElement(brand);

        Font subBrandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_SECONDARY);
        Paragraph subBrand = new Paragraph("ORIGEN VILLA RICA · RESERVA DE BIOSFERA UNESCO", subBrandFont);
        subBrand.setSpacingAfter(6);
        leftCell.addElement(subBrand);

        Font reportTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.BLACK);
        Paragraph title = new Paragraph(reportTitle, reportTitleFont);
        leftCell.addElement(title);

        headerTable.addCell(leftCell);

        // Columna derecha: Metadatos
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.setPadding(0);

        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXT_MUTED);
        Font metaBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.DARK_GRAY);

        Paragraph meta = new Paragraph();
        meta.setAlignment(Element.ALIGN_RIGHT);
        meta.add(new Chunk("Período: ", metaFont));
        meta.add(new Chunk(metricas.getRangoTexto() != null ? metricas.getRangoTexto() : "Histórico General", metaBold));
        meta.add(Chunk.NEWLINE);
        meta.add(new Chunk("Generado: ", metaFont));
        meta.add(new Chunk(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), metaBold));
        meta.add(Chunk.NEWLINE);
        meta.add(new Chunk("Por: ", metaFont));
        meta.add(new Chunk(adminEmail != null ? adminEmail : "Administrador", metaBold));
        rightCell.addElement(meta);

        headerTable.addCell(rightCell);
        doc.add(headerTable);

        // Línea divisoria decorativa
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setFixedHeight(2f);
        lineCell.setBackgroundColor(COLOR_PRIMARY);
        lineCell.setBorder(Rectangle.NO_BORDER);
        line.addCell(lineCell);
        line.setSpacingAfter(15);
        doc.add(line);
    }

    private PdfPCell crearKpiCell(String titulo, String valor, String subtitulo) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_BG_HEADER);
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(8);

        Font tFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_TEXT_MUTED);
        Paragraph t = new Paragraph(titulo, tFont);
        t.setSpacingAfter(3);
        cell.addElement(t);

        Font vFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, COLOR_PRIMARY);
        Paragraph v = new Paragraph(valor, vFont);
        v.setSpacingAfter(2);
        cell.addElement(v);

        Font sFont = FontFactory.getFont(FontFactory.HELVETICA, 7, COLOR_TEXT_MUTED);
        Paragraph s = new Paragraph(subtitulo, sFont);
        cell.addElement(s);

        return cell;
    }

    private PdfPCell crearHeaderCell(String texto, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        cell.setBackgroundColor(COLOR_PRIMARY);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(5);
        cell.setPaddingBottom(5);
        cell.setBorderColor(COLOR_PRIMARY);
        return cell;
    }

    private PdfPCell crearDataCell(String texto, Color bg, int align, boolean bold) {
        Font font = FontFactory.getFont(bold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, 8, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(4);
        cell.setPaddingBottom(4);
        cell.setBorderColor(COLOR_BORDER);
        return cell;
    }

    // Pie de página con numeración
    private static class HeaderFooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(2);
            footer.setTotalWidth(document.right() - document.left());
            footer.setWidthPercentage(100);

            Font font = FontFactory.getFont(FontFactory.HELVETICA, 7, COLOR_TEXT_MUTED);

            PdfPCell left = new PdfPCell(new Phrase("Café Valle Blanco · Villa Rica, Pasco, Perú · RUC: 20601234567", font));
            left.setBorder(Rectangle.NO_BORDER);
            left.setHorizontalAlignment(Element.ALIGN_LEFT);
            footer.addCell(left);

            PdfPCell right = new PdfPCell(new Phrase("Página " + writer.getPageNumber(), font));
            right.setBorder(Rectangle.NO_BORDER);
            right.setHorizontalAlignment(Element.ALIGN_RIGHT);
            footer.addCell(right);

            footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 10, writer.getDirectContent());
        }
    }
}
