package com.valleblanco.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.valleblanco.backend.dto.PedidoItemResponse;
import com.valleblanco.backend.dto.PedidoResponse;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
public class ComprobantePdfService {

    private static final Color COLOR_PRIMARY    = new Color(27, 67, 50);     // #1B4332
    private static final Color COLOR_SECONDARY  = new Color(184, 115, 51);   // #B87333
    private static final Color COLOR_BG_HEADER  = new Color(245, 245, 240);
    private static final Color COLOR_ROW_ALT    = new Color(250, 250, 247);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 100, 100);
    private static final Color COLOR_BORDER     = new Color(220, 220, 215);

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generarComprobante(PedidoResponse pedido) {
        Document document = new Document(PageSize.A4, 36, 36, 40, 48);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterPageEvent());
            document.open();

            // 1. CABECERA
            agregarCabecera(document, pedido);

            // 2. DATOS DEL CLIENTE Y ENTREGA
            agregarDatosCliente(document, pedido);

            // 3. DETALLE DE PRODUCTOS
            agregarTablaProductos(document, pedido);

            // 4. RESUMEN DE TOTALES
            agregarTotales(document, pedido);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el comprobante PDF: " + e.getMessage(), e);
        }
    }

    private void agregarCabecera(Document doc, PedidoResponse pedido) throws DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{60f, 40f});
        header.setSpacingAfter(14);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(0);

        Paragraph brand = new Paragraph("CAFÉ VALLE BLANCO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARY));
        brand.setSpacingAfter(2);
        left.addElement(brand);

        Paragraph sub = new Paragraph("ORIGEN VILLA RICA · OXAPAMPA, PASCO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, COLOR_SECONDARY));
        sub.setSpacingAfter(6);
        left.addElement(sub);

        Paragraph ruc = new Paragraph("RUC: 20601234567 · contacto@valleblanco.pe", FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXT_MUTED));
        left.addElement(ruc);
        header.addCell(left);

        // Cuadro de Comprobante a la derecha
        PdfPCell right = new PdfPCell();
        right.setBorderColor(COLOR_PRIMARY);
        right.setBorderWidth(1.2f);
        right.setBackgroundColor(COLOR_BG_HEADER);
        right.setPadding(10);
        right.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph docTipo = new Paragraph("COMPROBANTE DE PEDIDO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PRIMARY));
        docTipo.setAlignment(Element.ALIGN_CENTER);
        docTipo.setSpacingAfter(4);
        right.addElement(docTipo);

        Paragraph num = new Paragraph(pedido.getNumeroPedido() != null ? pedido.getNumeroPedido() : "#" + pedido.getId(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.BLACK));
        num.setAlignment(Element.ALIGN_CENTER);
        num.setSpacingAfter(3);
        right.addElement(num);

        String fechaStr = pedido.getCreadoEn() != null ? pedido.getCreadoEn().format(dateFormat) : "-";
        Paragraph fecha = new Paragraph("Emitido: " + fechaStr, FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXT_MUTED));
        fecha.setAlignment(Element.ALIGN_CENTER);
        right.addElement(fecha);

        header.addCell(right);
        doc.add(header);

        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell lc = new PdfPCell();
        lc.setFixedHeight(2f);
        lc.setBackgroundColor(COLOR_PRIMARY);
        lc.setBorder(Rectangle.NO_BORDER);
        line.addCell(lc);
        line.setSpacingAfter(14);
        doc.add(line);
    }

    private void agregarDatosCliente(Document doc, PedidoResponse pedido) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PRIMARY);
        Paragraph title = new Paragraph("DATOS DE CLIENTE Y ENTREGA", sectionFont);
        title.setSpacingAfter(6);
        doc.add(title);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{50f, 50f});
        infoTable.setSpacingAfter(16);

        String cliente = pedido.getDirDestinatario() != null && !pedido.getDirDestinatario().isBlank()
                ? pedido.getDirDestinatario()
                : (pedido.getClienteNombre() != null ? pedido.getClienteNombre() : "Cliente");

        String direccion = String.format("%s%s, %s, %s, %s",
                pedido.getDirLinea1() != null ? pedido.getDirLinea1() : "Sin dirección",
                (pedido.getDirLinea2() != null && !pedido.getDirLinea2().isBlank()) ? " (" + pedido.getDirLinea2() + ")" : "",
                pedido.getDirDistrito() != null ? pedido.getDirDistrito() : "-",
                pedido.getDirProvincia() != null ? pedido.getDirProvincia() : "-",
                pedido.getDirDepartamento() != null ? pedido.getDirDepartamento() : "-"
        );

        infoTable.addCell(crearInfoCell("Destinatario / Cliente", cliente));
        infoTable.addCell(crearInfoCell("Estado del Pedido", pedido.getEstado() != null ? pedido.getEstado() : "-"));
        infoTable.addCell(crearInfoCell("Teléfono de Contacto", pedido.getDirTelefono() != null ? pedido.getDirTelefono() : "No registrado"));
        infoTable.addCell(crearInfoCell("Método de Pago", pedido.getMetodoPago() != null ? pedido.getMetodoPago() : "No especificado"));

        PdfPCell dirCell = crearInfoCell("Dirección de Entrega", direccion);
        dirCell.setColspan(2);
        infoTable.addCell(dirCell);

        if (pedido.getNotas() != null && !pedido.getNotas().isBlank()) {
            PdfPCell notasCell = crearInfoCell("Notas del Pedido", pedido.getNotas());
            notasCell.setColspan(2);
            infoTable.addCell(notasCell);
        }

        doc.add(infoTable);
    }

    private PdfPCell crearInfoCell(String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(COLOR_BORDER);
        cell.setBackgroundColor(Color.WHITE);
        cell.setPadding(7);

        Paragraph l = new Paragraph(label.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, COLOR_TEXT_MUTED));
        l.setSpacingAfter(2);
        cell.addElement(l);

        Paragraph v = new Paragraph(value != null ? value : "-", FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK));
        cell.addElement(v);
        return cell;
    }

    private void agregarTablaProductos(Document doc, PedidoResponse pedido) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PRIMARY);
        Paragraph title = new Paragraph("DETALLE DE PRODUCTOS", sectionFont);
        title.setSpacingAfter(6);
        doc.add(title);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{7f, 41f, 14f, 10f, 14f, 14f});
        table.setSpacingAfter(14);

        table.addCell(crearHeaderCell("#", Element.ALIGN_CENTER));
        table.addCell(crearHeaderCell("Producto", Element.ALIGN_LEFT));
        table.addCell(crearHeaderCell("Peso", Element.ALIGN_RIGHT));
        table.addCell(crearHeaderCell("Cant.", Element.ALIGN_CENTER));
        table.addCell(crearHeaderCell("P. Unit.", Element.ALIGN_RIGHT));
        table.addCell(crearHeaderCell("Subtotal", Element.ALIGN_RIGHT));

        int idx = 1;
        if (pedido.getItems() != null) {
            for (PedidoItemResponse item : pedido.getItems()) {
                Color bg = (idx % 2 == 0) ? COLOR_ROW_ALT : Color.WHITE;
                String nombre = item.getNombreProducto()
                        + (item.getNombreSecundario() != null && !item.getNombreSecundario().isBlank() ? " — " + item.getNombreSecundario() : "");

                table.addCell(crearDataCell(String.valueOf(idx), bg, Element.ALIGN_CENTER, true));
                table.addCell(crearDataCell(nombre, bg, Element.ALIGN_LEFT, false));
                table.addCell(crearDataCell((item.getPeso() != null ? item.getPeso() : 0) + " g", bg, Element.ALIGN_RIGHT, false));
                table.addCell(crearDataCell(String.valueOf(item.getCantidad() != null ? item.getCantidad() : 0), bg, Element.ALIGN_CENTER, false));
                table.addCell(crearDataCell("S/ " + moneyFormat.format(item.getPrecioUnitario() != null ? item.getPrecioUnitario() : BigDecimal.ZERO), bg, Element.ALIGN_RIGHT, false));
                table.addCell(crearDataCell("S/ " + moneyFormat.format(item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO), bg, Element.ALIGN_RIGHT, true));
                idx++;
            }
        }

        doc.add(table);
    }

    private void agregarTotales(Document doc, PedidoResponse pedido) throws DocumentException {
        PdfPTable totalsWrapper = new PdfPTable(2);
        totalsWrapper.setWidthPercentage(100);
        totalsWrapper.setWidths(new float[]{60f, 40f});

        PdfPCell spacer = new PdfPCell(new Phrase("Gracias por elegir Café Valle Blanco.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8.5f, COLOR_TEXT_MUTED)));
        spacer.setBorder(Rectangle.NO_BORDER);
        spacer.setVerticalAlignment(Element.ALIGN_BOTTOM);
        totalsWrapper.addCell(spacer);

        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(100);
        totalsTable.setWidths(new float[]{55f, 45f});

        BigDecimal subtotal = pedido.getSubtotal() != null ? pedido.getSubtotal() : BigDecimal.ZERO;
        BigDecimal envio = pedido.getCostoEnvio() != null ? pedido.getCostoEnvio() : BigDecimal.ZERO;
        BigDecimal total = pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO;

        totalsTable.addCell(crearDataCell("Subtotal", COLOR_ROW_ALT, Element.ALIGN_LEFT, false));
        totalsTable.addCell(crearDataCell("S/ " + moneyFormat.format(subtotal), COLOR_ROW_ALT, Element.ALIGN_RIGHT, false));

        totalsTable.addCell(crearDataCell("Costo de envío", Color.WHITE, Element.ALIGN_LEFT, false));
        totalsTable.addCell(crearDataCell("S/ " + moneyFormat.format(envio), Color.WHITE, Element.ALIGN_RIGHT, false));

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        totalLabel.setBackgroundColor(COLOR_PRIMARY);
        totalLabel.setPadding(7);
        totalLabel.setBorderColor(COLOR_PRIMARY);
        totalsTable.addCell(totalLabel);

        PdfPCell totalVal = new PdfPCell(new Phrase("S/ " + moneyFormat.format(total), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        totalVal.setBackgroundColor(COLOR_PRIMARY);
        totalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalVal.setPadding(7);
        totalVal.setBorderColor(COLOR_PRIMARY);
        totalsTable.addCell(totalVal);

        PdfPCell rightWrap = new PdfPCell(totalsTable);
        rightWrap.setBorder(Rectangle.NO_BORDER);
        rightWrap.setPadding(0);
        totalsWrapper.addCell(rightWrap);

        doc.add(totalsWrapper);
    }

    private PdfPCell crearHeaderCell(String texto, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        cell.setBackgroundColor(COLOR_PRIMARY);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setBorderColor(COLOR_PRIMARY);
        return cell;
    }

    private PdfPCell crearDataCell(String texto, Color bg, int align, boolean bold) {
        Font font = FontFactory.getFont(bold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, 8.5f, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorderColor(COLOR_BORDER);
        return cell;
    }

    private static class FooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(2);
            footer.setTotalWidth(document.right() - document.left());
            footer.setWidthPercentage(100);

            Font font = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, COLOR_TEXT_MUTED);

            PdfPCell left = new PdfPCell(new Phrase("Café Valle Blanco · Comprobante de Pedido · Villa Rica, Pasco, Perú", font));
            left.setBorder(Rectangle.NO_BORDER);
            left.setHorizontalAlignment(Element.ALIGN_LEFT);
            footer.addCell(left);

            PdfPCell right = new PdfPCell(new Phrase("Página " + writer.getPageNumber(), font));
            right.setBorder(Rectangle.NO_BORDER);
            right.setHorizontalAlignment(Element.ALIGN_RIGHT);
            footer.addCell(right);

            footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 12, writer.getDirectContent());
        }
    }
}
