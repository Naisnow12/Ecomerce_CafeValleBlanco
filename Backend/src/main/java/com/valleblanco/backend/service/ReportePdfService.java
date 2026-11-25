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
import java.util.Map;

@Service
public class ReportePdfService {

    private static final Color COLOR_PRIMARY = new Color(27, 67, 50);       // #1B4332 Verde Bosque
    private static final Color COLOR_SECONDARY = new Color(184, 115, 51);   // #B87333 Cobre / Dorado
    private static final Color COLOR_BG_HEADER = new Color(245, 245, 240);   // Gris claro
    private static final Color COLOR_ROW_ALT = new Color(250, 250, 247);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 100, 100);
    private static final Color COLOR_BORDER = new Color(220, 220, 215);

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generarReportePdf(ReporteMetricasResponse metricas, String adminEmail) {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());
            document.open();

            // 1. ENCABEZADO OFICIAL
            agregarCabecera(document, metricas, adminEmail);

            // 2. RESUMEN DE KPIS
            agregarSeccionKpis(document, metricas);

            // 3. TOP PRODUCTOS
            agregarTopProductos(document, metricas);

            // 4. MÉTODOS DE PAGO Y ESTADOS
            agregarMetodosYEstados(document, metricas);

            // 5. VENTAS POR DEPARTAMENTO
            agregarVentasPorDepartamento(document, metricas);

            // 6. ALERTAS DE STOCK (Si las hay)
            if (metricas.getStockCritico() != null && !metricas.getStockCritico().isEmpty()) {
                agregarAlertasStock(document, metricas);
            }

            // 6. DETALLE DE PEDIDOS DEL PERÍODO
            if (metricas.getPedidos() != null && !metricas.getPedidos().isEmpty()) {
                agregarListadoPedidos(document, metricas);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte PDF: " + e.getMessage(), e);
        }
    }

    private void agregarCabecera(Document doc, ReporteMetricasResponse metricas, String adminEmail) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{65f, 35f});
        headerTable.setSpacingAfter(15);

        // Columna Izquierda: Marca y Título
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
        Paragraph title = new Paragraph("REPORTE EJECUTIVO DE VENTAS Y RENDIMIENTO", reportTitleFont);
        leftCell.addElement(title);

        headerTable.addCell(leftCell);

        // Columna Derecha: Metadatos
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
        meta.add(new Chunk(LocalDateTime.now().format(dateFormat), metaBold));
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

    private void agregarSeccionKpis(Document doc, ReporteMetricasResponse metricas) throws DocumentException {
        Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);
        Paragraph title = new Paragraph("1. INDICADORES CLAVE DE DESEMPEÑO (KPIs)", sectionTitleFont);
        title.setSpacingAfter(8);
        doc.add(title);

        PdfPTable kpiTable = new PdfPTable(4);
        kpiTable.setWidthPercentage(100);
        kpiTable.setWidths(new float[]{25f, 25f, 25f, 25f});
        kpiTable.setSpacingAfter(15);

        String ingresos = "S/ " + (metricas.getTotalIngresos() != null ? moneyFormat.format(metricas.getTotalIngresos()) : "0.00");
        String pedidos = String.valueOf(metricas.getTotalPedidos());
        String ticket = "S/ " + (metricas.getTicketPromedio() != null ? moneyFormat.format(metricas.getTicketPromedio()) : "0.00");
        String unidades = String.valueOf(metricas.getTotalUnidadesVendidas());

        kpiTable.addCell(crearKpiCell("INGRESOS TOTALES", ingresos, "Pedidos válidos no cancelados"));
        kpiTable.addCell(crearKpiCell("TOTAL PEDIDOS", pedidos, metricas.getPedidosCompletados() + " completados"));
        kpiTable.addCell(crearKpiCell("TICKET PROMEDIO", ticket, "Venta promedio por pedido"));
        kpiTable.addCell(crearKpiCell("BOLSAS VENDIDAS", unidades, "Unidades de café despachadas"));

        doc.add(kpiTable);
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

    private void agregarTopProductos(Document doc, ReporteMetricasResponse metricas) throws DocumentException {
        if (metricas.getTopProductos() == null || metricas.getTopProductos().isEmpty()) return;

        Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);
        Paragraph title = new Paragraph("2. PRODUCTOS MÁS VENDIDOS (TOP)", sectionTitleFont);
        title.setSpacingAfter(8);
        doc.add(title);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{8f, 48f, 16f, 16f, 12f});
        table.setSpacingAfter(15);

        // Header
        table.addCell(crearHeaderCell("#", Element.ALIGN_CENTER));
        table.addCell(crearHeaderCell("Producto", Element.ALIGN_LEFT));
        table.addCell(crearHeaderCell("Unidades", Element.ALIGN_RIGHT));
        table.addCell(crearHeaderCell("Total (S/)", Element.ALIGN_RIGHT));
        table.addCell(crearHeaderCell("Part. %", Element.ALIGN_RIGHT));

        int rank = 1;
        for (ReporteMetricasResponse.TopProductoReporte prod : metricas.getTopProductos()) {
            Color rowColor = (rank % 2 == 0) ? COLOR_ROW_ALT : Color.WHITE;
            table.addCell(crearDataCell(String.valueOf(rank), rowColor, Element.ALIGN_CENTER, true));
            table.addCell(crearDataCell(prod.getNombre(), rowColor, Element.ALIGN_LEFT, false));
            table.addCell(crearDataCell(String.valueOf(prod.getUnidadesVendidas()), rowColor, Element.ALIGN_RIGHT, false));
            table.addCell(crearDataCell("S/ " + moneyFormat.format(prod.getTotalGenerado()), rowColor, Element.ALIGN_RIGHT, false));
            table.addCell(crearDataCell(String.format("%.1f%%", prod.getPorcentaje()), rowColor, Element.ALIGN_RIGHT, false));
            rank++;
        }

        doc.add(table);
    }

    private void agregarMetodosYEstados(Document doc, ReporteMetricasResponse metricas) throws DocumentException {
        Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);
        Paragraph title = new Paragraph("3. DESGLOSE OPERATIVO Y FINANCIERO", sectionTitleFont);
        title.setSpacingAfter(8);
        doc.add(title);

        PdfPTable container = new PdfPTable(2);
        container.setWidthPercentage(100);
        container.setWidths(new float[]{50f, 50f});
        container.setSpacingAfter(15);

        // Columna 1: Métodos de Pago
        PdfPCell col1 = new PdfPCell();
        col1.setBorder(Rectangle.NO_BORDER);
        col1.setPaddingRight(6);

        PdfPTable metodosTable = new PdfPTable(2);
        metodosTable.setWidthPercentage(100);
        metodosTable.setWidths(new float[]{60f, 40f});
        metodosTable.addCell(crearHeaderCell("Método de Pago", Element.ALIGN_LEFT));
        metodosTable.addCell(crearHeaderCell("Ventas (S/)", Element.ALIGN_RIGHT));

        if (metricas.getVentasPorMetodoPago() != null && !metricas.getVentasPorMetodoPago().isEmpty()) {
            boolean alt = false;
            for (Map.Entry<String, BigDecimal> entry : metricas.getVentasPorMetodoPago().entrySet()) {
                Color c = alt ? COLOR_ROW_ALT : Color.WHITE;
                metodosTable.addCell(crearDataCell(entry.getKey(), c, Element.ALIGN_LEFT, false));
                metodosTable.addCell(crearDataCell("S/ " + moneyFormat.format(entry.getValue()), c, Element.ALIGN_RIGHT, false));
                alt = !alt;
            }
        } else {
            metodosTable.addCell(crearDataCell("Sin datos", Color.WHITE, Element.ALIGN_LEFT, false));
            metodosTable.addCell(crearDataCell("S/ 0.00", Color.WHITE, Element.ALIGN_RIGHT, false));
        }
        col1.addElement(metodosTable);
        container.addCell(col1);

        // Columna 2: Estados de Pedidos
        PdfPCell col2 = new PdfPCell();
        col2.setBorder(Rectangle.NO_BORDER);
        col2.setPaddingLeft(6);

        PdfPTable estadosTable = new PdfPTable(2);
        estadosTable.setWidthPercentage(100);
        estadosTable.setWidths(new float[]{60f, 40f});
        estadosTable.addCell(crearHeaderCell("Estado del Pedido", Element.ALIGN_LEFT));
        estadosTable.addCell(crearHeaderCell("Cantidad", Element.ALIGN_RIGHT));

        if (metricas.getPedidosPorEstado() != null && !metricas.getPedidosPorEstado().isEmpty()) {
            boolean alt = false;
            for (Map.Entry<String, Long> entry : metricas.getPedidosPorEstado().entrySet()) {
                Color c = alt ? COLOR_ROW_ALT : Color.WHITE;
                estadosTable.addCell(crearDataCell(entry.getKey(), c, Element.ALIGN_LEFT, false));
                estadosTable.addCell(crearDataCell(String.valueOf(entry.getValue()), c, Element.ALIGN_RIGHT, false));
                alt = !alt;
            }
        } else {
            estadosTable.addCell(crearDataCell("Sin pedidos", Color.WHITE, Element.ALIGN_LEFT, false));
            estadosTable.addCell(crearDataCell("0", Color.WHITE, Element.ALIGN_RIGHT, false));
        }
        col2.addElement(estadosTable);
        container.addCell(col2);

        doc.add(container);
    }

    private void agregarVentasPorDepartamento(Document doc, ReporteMetricasResponse metricas) throws DocumentException {
        if (metricas.getVentasPorDepartamento() == null || metricas.getVentasPorDepartamento().isEmpty()) return;

        Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);
        Paragraph title = new Paragraph("4. DEPARTAMENTOS CON MAYOR VENTA DE CAFÉ", sectionTitleFont);
        title.setSpacingAfter(8);
        doc.add(title);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{38f, 15f, 15f, 20f, 12f});
        table.setSpacingAfter(15);

        table.addCell(crearHeaderCell("Departamento", Element.ALIGN_LEFT));
        table.addCell(crearHeaderCell("Pedidos", Element.ALIGN_RIGHT));
        table.addCell(crearHeaderCell("Unidades", Element.ALIGN_RIGHT));
        table.addCell(crearHeaderCell("Total (S/)", Element.ALIGN_RIGHT));
        table.addCell(crearHeaderCell("Part. %", Element.ALIGN_RIGHT));

        boolean alt = false;
        for (ReporteMetricasResponse.VentaDepartamentoReporte dep : metricas.getVentasPorDepartamento()) {
            Color rowColor = alt ? COLOR_ROW_ALT : Color.WHITE;
            table.addCell(crearDataCell(dep.getDepartamento(), rowColor, Element.ALIGN_LEFT, true));
            table.addCell(crearDataCell(String.valueOf(dep.getCantidadPedidos()), rowColor, Element.ALIGN_RIGHT, false));
            table.addCell(crearDataCell(String.valueOf(dep.getUnidadesVendidas()), rowColor, Element.ALIGN_RIGHT, false));
            table.addCell(crearDataCell("S/ " + moneyFormat.format(dep.getTotalGenerado()), rowColor, Element.ALIGN_RIGHT, false));
            table.addCell(crearDataCell(String.format("%.1f%%", dep.getPorcentaje()), rowColor, Element.ALIGN_RIGHT, false));
            alt = !alt;
        }

        doc.add(table);
    }

    private void agregarAlertasStock(Document doc, ReporteMetricasResponse metricas) throws DocumentException {
        Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(180, 50, 50));
        Paragraph title = new Paragraph("4. ALERTAS DE INVENTARIO / STOCK CRÍTICO", sectionTitleFont);
        title.setSpacingAfter(8);
        doc.add(title);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{50f, 18f, 18f, 14f});
        table.setSpacingAfter(15);

        table.addCell(crearHeaderCell("Producto", Element.ALIGN_LEFT));
        table.addCell(crearHeaderCell("Stock Actual", Element.ALIGN_RIGHT));
        table.addCell(crearHeaderCell("Nivel Mínimo", Element.ALIGN_RIGHT));
        table.addCell(crearHeaderCell("Estado", Element.ALIGN_CENTER));

        boolean alt = false;
        for (ReporteMetricasResponse.StockCriticoReporte sc : metricas.getStockCritico()) {
            Color rowColor = alt ? COLOR_ROW_ALT : Color.WHITE;
            table.addCell(crearDataCell(sc.getNombre(), rowColor, Element.ALIGN_LEFT, false));
            table.addCell(crearDataCell(String.valueOf(sc.getStockActual()), rowColor, Element.ALIGN_RIGHT, false));
            table.addCell(crearDataCell(String.valueOf(sc.getNivelMinimo()), rowColor, Element.ALIGN_RIGHT, false));
            table.addCell(crearDataCell(sc.getEstado(), rowColor, Element.ALIGN_CENTER, true));
            alt = !alt;
        }

        doc.add(table);
    }

    private void agregarListadoPedidos(Document doc, ReporteMetricasResponse metricas) throws DocumentException {
        Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);
        Paragraph title = new Paragraph("5. LISTADO DE PEDIDOS DEL PERÍODO (MUESTRA RECIENTE)", sectionTitleFont);
        title.setSpacingAfter(8);
        doc.add(title);

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

        int count = 0;
        boolean alt = false;
        for (ReporteMetricasResponse.PedidoResumenReporte ped : metricas.getPedidos()) {
            if (count >= 25) break; // Límite para evitar reportes excesivamente largos en una sola página
            Color rowColor = alt ? COLOR_ROW_ALT : Color.WHITE;
            table.addCell(crearDataCell(ped.getNumeroPedido(), rowColor, Element.ALIGN_LEFT, true));
            table.addCell(crearDataCell(ped.getFecha() != null ? ped.getFecha().format(dateFormat) : "-", rowColor, Element.ALIGN_LEFT, false));
            table.addCell(crearDataCell(ped.getCliente() != null ? ped.getCliente() : "Cliente", rowColor, Element.ALIGN_LEFT, false));
            table.addCell(crearDataCell(ped.getMetodoPago() != null ? ped.getMetodoPago() : "-", rowColor, Element.ALIGN_LEFT, false));
            table.addCell(crearDataCell("S/ " + moneyFormat.format(ped.getTotal()), rowColor, Element.ALIGN_RIGHT, false));
            table.addCell(crearDataCell(ped.getEstado(), rowColor, Element.ALIGN_CENTER, false));
            alt = !alt;
            count++;
        }

        doc.add(table);
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
