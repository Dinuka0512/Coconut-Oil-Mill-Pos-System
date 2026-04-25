package com.dev09.kumaracoconutoil.util;

import com.dev09.kumaracoconutoil.entity.CustomerEntity;
import com.dev09.kumaracoconutoil.entity.ProductEntity;
import com.dev09.kumaracoconutoil.entity.OrderEntity;
import com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfGenerator {

    private static final float MM_TO_POINTS = 2.83465f;
    private static final float A4_WIDTH = 210f * MM_TO_POINTS;
    private static final float A4_HEIGHT = 297f * MM_TO_POINTS;
    private static final float MARGIN = 25f * MM_TO_POINTS;

    private static final String COMPANY_NAME = "DILSHAN OIL MILL";
    private static final String COMPANY_TAGLINE = "Your Trusted Partner in Coconut Products";
    private static final String COMPANY_ADDRESS = "309/a 5 Baddegoda Rd, Bandaragama 12530";
    private static final String COMPANY_PHONE = "Tel: 077-6518819 | 012-3456789";
    private static final String DEVELOPED_BY = "Developed by Dev09Solutions.com";

    // Colors
    private static final float[] COLOR_DARK = {0.12f, 0.12f, 0.12f};
    private static final float[] COLOR_LIGHT_GRAY = {0.96f, 0.96f, 0.96f};
    private static final float[] COLOR_WHITE = {1.0f, 1.0f, 1.0f};
    private static final float[] COLOR_BLACK = {0.0f, 0.0f, 0.0f};

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
    private static final DateTimeFormatter BILL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ================================
    // 1. CUSTOMER REPORT
    // ================================
    public static byte[] AllCustomerReport(List<CustomerEntity> customers) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(new PDRectangle(A4_WIDTH, A4_HEIGHT));
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);

            float margin = MARGIN;
            float contentWidth = A4_WIDTH - 2 * margin;
            float y = A4_HEIGHT - margin;

            drawCompanyHeader(content, y);
            y -= 105;

            // Report Title
            drawCenteredText(content, "CUSTOMER REPORT", A4_WIDTH, y, 16, PDType1Font.HELVETICA_BOLD);
            y -= 28;

            // Table Header
            float colId = margin + 12;
            float colName = margin + 85;
            float colContact = margin + 255;

            drawTableHeader(content, y, margin, contentWidth,
                    new String[]{"Customer ID", "Customer Name", "Contact Number"},
                    new float[]{colId, colName, colContact});

            y -= 18;

            // Table Body
            content.setFont(PDType1Font.HELVETICA, 10);
            int row = 0;

            for (CustomerEntity c : customers) {
                if (y < margin + 60) break;

                content.setNonStrokingColor(COLOR_DARK[0], COLOR_DARK[1], COLOR_DARK[2]);

                String id = c.getCustId() != null ? c.getCustId() : "-";
                String name = truncate(c.getName() != null ? c.getName() : "-", 38);
                String contact = c.getContact() != null ? c.getContact() : "-";

                drawCenteredTextInColumn(content, id, colId, colName - colId - 10, y - 4);
                drawCenteredTextInColumn(content, name, colName, colContact - colName - 10, y - 4);
                drawCenteredTextInColumn(content, contact, colContact, A4_WIDTH - margin - colContact - 12, y - 4);

                content.setLineWidth(0.3f);
                content.setStrokingColor(COLOR_DARK[0], COLOR_DARK[1], COLOR_DARK[2]);
                content.moveTo(margin, y - 12);
                content.lineTo(A4_WIDTH - margin, y - 12);
                content.stroke();

                y -= 15;
                row++;
            }

            drawFooter(content, y - 25, margin, "Total Customers: " + customers.size());

            content.close();
            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================================
    // 2. PRODUCT REPORT
    // ================================
    public static byte[] AllProductReport(List<ProductEntity> products) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(new PDRectangle(A4_WIDTH, A4_HEIGHT));
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);

            float margin = MARGIN;
            float contentWidth = A4_WIDTH - 2 * margin;
            float y = A4_HEIGHT - margin;

            drawCompanyHeader(content, y);
            y -= 105;

            drawCenteredText(content, "PRODUCT REPORT", A4_WIDTH, y, 16, PDType1Font.HELVETICA_BOLD);
            y -= 28;

            // Table columns
            float colId = margin + 12;
            float colName = margin + 85;
            float colPrice = margin + 265;
            float colStock = margin + 370;

            drawTableHeader(content, y, margin, contentWidth,
                    new String[]{"Product ID", "Product Name", "Unit Price", "Stock Qty"},
                    new float[]{colId, colName, colPrice, colStock});

            y -= 18;

            content.setFont(PDType1Font.HELVETICA, 10);
            int row = 0;
            int lowStock = 0;

            for (ProductEntity p : products) {
                if (y < margin + 60) break;

                boolean isLowStock = p.getQty() < 10;
                content.setNonStrokingColor(isLowStock ? 0.0f : COLOR_DARK[0],
                        isLowStock ? 0.0f : COLOR_DARK[1],
                        isLowStock ? 0.0f : COLOR_DARK[2]);

                if (isLowStock) lowStock++;

                String id = p.getId() != null ? p.getId() : "-";
                String name = truncate(p.getName() != null ? p.getName() : "-", 35);
                String price = String.format("%,.2f", p.getPrice());
                String stock = String.valueOf(p.getQty());

                drawCenteredTextInColumn(content, id, colId, colName - colId - 10, y - 4);
                drawCenteredTextInColumn(content, name, colName, colPrice - colName - 10, y - 4);
                drawCenteredTextInColumn(content, price, colPrice, colStock - colPrice - 10, y - 4);
                drawCenteredTextInColumn(content, stock, colStock, A4_WIDTH - margin - colStock - 12, y - 4);

                content.setLineWidth(0.3f);
                content.setStrokingColor(COLOR_DARK[0], COLOR_DARK[1], COLOR_DARK[2]);
                content.moveTo(margin, y - 12);
                content.lineTo(A4_WIDTH - margin, y - 12);
                content.stroke();

                y -= 15;
                row++;
            }

            drawFooter(content, y - 25, margin, "Total Products: " + products.size() + "   |   Low Stock: " + lowStock);

            content.close();
            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================================
    // Helper Methods
    // ================================

    private static void drawCompanyHeader(PDPageContentStream content, float y) throws IOException {
        content.setNonStrokingColor(COLOR_DARK[0], COLOR_DARK[1], COLOR_DARK[2]);

        // Company Name
        drawCenteredText(content, COMPANY_NAME, A4_WIDTH, y, 19, PDType1Font.HELVETICA_BOLD);
        y -= 22;

        // Tagline
        content.setFont(PDType1Font.HELVETICA, 10);
        drawCenteredText(content, COMPANY_TAGLINE, A4_WIDTH, y, 10, PDType1Font.HELVETICA);
        y -= 16;

        // Address & Phone
        content.setFont(PDType1Font.HELVETICA, 9);
        drawCenteredText(content, COMPANY_ADDRESS, A4_WIDTH, y, 9, PDType1Font.HELVETICA);
        y -= 13;
        drawCenteredText(content, COMPANY_PHONE, A4_WIDTH, y, 9, PDType1Font.HELVETICA);

        // Divider
        y -= 22;
        content.setLineWidth(1.5f);
        content.moveTo(MARGIN, y);
        content.lineTo(A4_WIDTH - MARGIN, y);
        content.stroke();
    }

    private static void drawTableHeader(PDPageContentStream content, float y, float margin, float width,
                                        String[] headers, float[] colStarts) throws IOException {
        // Black background
        content.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        content.addRect(margin, y - 17, width, 17);
        content.fill();

        content.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
        content.setFont(PDType1Font.HELVETICA_BOLD, 10);

        for (int i = 0; i < headers.length; i++) {
            float colWidth = (i < headers.length - 1) ?
                    colStarts[i + 1] - colStarts[i] :
                    A4_WIDTH - MARGIN - colStarts[i];

            drawCenteredTextInColumn(content, headers[i], colStarts[i], colWidth - 15, y - 5);
        }
    }

    /**
     * NEW METHOD: Properly centers text inside a column
     */
    private static void drawCenteredTextInColumn(PDPageContentStream content, String text,
                                                 float colStart, float colWidth, float y) throws IOException {
        if (text == null) text = "";
        float textWidth = getTextWidth(text, 10);
        float x = colStart + (colWidth - textWidth) / 2;
        content.beginText();
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
    }

    private static void drawCenteredText(PDPageContentStream content, String text, float pageWidth,
                                         float y, float fontSize, PDType1Font font) throws IOException {
        content.setFont(font, fontSize);
        float textWidth = getTextWidth(text, fontSize);
        content.beginText();
        content.newLineAtOffset((pageWidth - textWidth) / 2, y);
        content.showText(text);
        content.endText();
    }

    private static void drawFooter(PDPageContentStream content, float y, float margin, String footerText) throws IOException {
        content.setLineWidth(1f);
        content.moveTo(margin, y + 15);
        content.lineTo(A4_WIDTH - margin, y + 15);
        content.stroke();

        y -= 12;
        content.setFont(PDType1Font.HELVETICA, 9);
        content.setNonStrokingColor(COLOR_DARK[0], COLOR_DARK[1], COLOR_DARK[2]);
        content.beginText();
        content.newLineAtOffset(margin + 8, y);
        content.showText(footerText);
        content.endText();

        String generated = "Generated: " + LocalDateTime.now().format(DATE_FORMAT);
        float genWidth = getTextWidth(generated, 9);
        content.beginText();
        content.newLineAtOffset(A4_WIDTH - margin - genWidth - 8, y);
        content.showText(generated);
        content.endText();

        y -= 18;
        content.setFont(PDType1Font.HELVETICA, 8);
        content.setNonStrokingColor(0.5f, 0.5f, 0.5f);
        drawCenteredText(content, DEVELOPED_BY, A4_WIDTH, y, 8, PDType1Font.HELVETICA);
    }

    private static float getTextWidth(String text, float fontSize) throws IOException {
        if (text == null) return 0;
        return (PDType1Font.HELVETICA.getStringWidth(text) / 1000f) * fontSize;
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max - 1) + "…" : text;
    }

    // ================================
    // 3. ORDER BILL
    // ================================
    public static byte[] generateOrderBill(OrderEntity order, List<OrderProductDetailsEntity> details) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(new PDRectangle(A4_WIDTH, A4_HEIGHT));
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);

            float margin = MARGIN;
            float y = A4_HEIGHT - margin;

            drawHeader(content, y, "INVOICE");

            y -= 25;
            content.setFont(PDType1Font.HELVETICA_BOLD, 10);
            content.setNonStrokingColor(COLOR_DARK[0], COLOR_DARK[1], COLOR_DARK[2]);
            content.beginText();
            content.newLineAtOffset(margin, y);
            content.showText("Order ID: " + order.getOrderId());
            content.endText();

            y -= 15;
            content.setFont(PDType1Font.HELVETICA, 9);
            content.beginText();
            content.newLineAtOffset(margin, y);
            content.showText("Date: " + order.getDate());
            content.endText();

            y -= 15;
            String custName = order.getCustomer() != null ? order.getCustomer().getName() : "-";
            content.beginText();
            content.newLineAtOffset(margin, y);
            content.showText("Customer: " + custName);
            content.endText();

            y -= 25;
            float[] cols = {margin + 10, margin + 180, margin + 260, margin + 340};
            drawTableHeader(content, y, cols, new String[]{"Product", "Qty", "Unit Price", "Total"});
            y -= 18;

            double grandTotal = 0;
            int row = 0;
            for (OrderProductDetailsEntity d : details) {
                if (y < margin + 60) break;
                
                double lineTotal = d.getQuantity() * d.getUnitPrice();
                grandTotal += lineTotal;
                String productName = d.getProduct() != null ? d.getProduct().getName() : "-";
                content.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
                drawTableRow(content, y - 4, cols, new String[]{
                    truncate(productName, 28),
                    String.valueOf(d.getQuantity()),
                    String.format("Rs. %.2f", d.getUnitPrice()),
                    String.format("Rs. %.2f", lineTotal)
                });

                content.setLineWidth(0.3f);
                content.setStrokingColor(COLOR_DARK[0], COLOR_DARK[1], COLOR_DARK[2]);
                content.moveTo(margin, y - 12);
                content.lineTo(A4_WIDTH - margin, y - 12);
                content.stroke();

                y -= 16;
                row++;
            }

            y -= 15;
            content.setFont(PDType1Font.HELVETICA_BOLD, 11);
            content.setNonStrokingColor(COLOR_DARK[0], COLOR_DARK[1], COLOR_DARK[2]);
            content.beginText();
            content.newLineAtOffset(margin + 200, y);
            content.showText("Grand Total: " + String.format("Rs. %.2f", grandTotal));
            content.endText();

            y -= 25;
            drawSimpleFooter(content, y, "Total Items: " + details.size());

            content.close();
            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generating order bill", e);
        }
    }

    private static void drawHeader(PDPageContentStream content, float y, String title) throws IOException {
        content.setFont(PDType1Font.HELVETICA_BOLD, 20);
        content.setNonStrokingColor(COLOR_DARK[0], COLOR_DARK[1], COLOR_DARK[2]);
        float w = getTextWidth(COMPANY_NAME, 20);
        content.beginText();
        content.newLineAtOffset((A4_WIDTH - w) / 2, y);
        content.showText(COMPANY_NAME);
        content.endText();

        y -= 22;
        content.setFont(PDType1Font.HELVETICA_OBLIQUE, 11);
        content.setNonStrokingColor(0.35f, 0.35f, 0.35f);
        w = getTextWidth(COMPANY_TAGLINE, 11);
        content.beginText();
        content.newLineAtOffset((A4_WIDTH - w) / 2, y);
        content.showText(COMPANY_TAGLINE);
        content.endText();

        y -= 18;
        content.setFont(PDType1Font.HELVETICA, 9);
        content.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        w = getTextWidth(COMPANY_ADDRESS, 9);
        content.beginText();
        content.newLineAtOffset((A4_WIDTH - w) / 2, y);
        content.showText(COMPANY_ADDRESS);
        content.endText();

        y -= 14;
        w = getTextWidth(COMPANY_PHONE, 9);
        content.beginText();
        content.newLineAtOffset((A4_WIDTH - w) / 2, y);
        content.showText(COMPANY_PHONE);
        content.endText();

        y -= 20;
        content.setLineWidth(1f);
        content.setStrokingColor(0.35f, 0.35f, 0.35f);
        content.moveTo(MARGIN, y);
        content.lineTo(A4_WIDTH - MARGIN, y);
        content.stroke();

        y -= 20;
        content.setFont(PDType1Font.HELVETICA_BOLD, 14);
        w = getTextWidth(title, 14);
        content.setNonStrokingColor(COLOR_DARK[0], COLOR_DARK[1], COLOR_DARK[2]);
        content.beginText();
        content.newLineAtOffset((A4_WIDTH - w) / 2, y);
        content.showText(title);
        content.endText();
    }

    private static void drawTableHeader(PDPageContentStream content, float y, float[] cols, String[] headers) throws IOException {
        content.setNonStrokingColor(COLOR_DARK[0], COLOR_DARK[1], COLOR_DARK[2]);
        content.addRect(MARGIN, y - 16, A4_WIDTH - 2 * MARGIN, 16);
        content.fill();

        content.setFont(PDType1Font.HELVETICA_BOLD, 9);
        content.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
        for (int i = 0; i < headers.length; i++) {
            content.beginText();
            content.newLineAtOffset(cols[i], y - 5);
            content.showText(headers[i]);
            content.endText();
        }
    }

    private static void drawTableRow(PDPageContentStream content, float y, float[] cols, String[] values) throws IOException {
        content.setFont(PDType1Font.HELVETICA, 8);
        for (int i = 0; i < values.length; i++) {
            content.beginText();
            content.newLineAtOffset(cols[i], y);
            content.showText(values[i]);
            content.endText();
        }
    }

    private static void drawFooter(PDPageContentStream content, float y, int count) throws IOException {
        content.setLineWidth(0.5f);
        content.setStrokingColor(0.35f, 0.35f, 0.35f);
        content.moveTo(MARGIN, y);
        content.lineTo(A4_WIDTH - MARGIN, y);
        content.stroke();

        y -= 18;
        content.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
        content.setNonStrokingColor(COLOR_DARK[0], COLOR_DARK[1], COLOR_DARK[2]);
        content.beginText();
        content.newLineAtOffset(MARGIN, y);
        content.showText("Total: " + count);
        content.endText();

        String now = LocalDateTime.now().format(DATE_FORMAT);
        float w = getTextWidth("Generated: " + now, 9);
        content.beginText();
        content.newLineAtOffset(A4_WIDTH - MARGIN - w, y);
        content.showText("Generated: " + now);
        content.endText();

        y -= 15;
        content.setFont(PDType1Font.HELVETICA, 8);
        content.setNonStrokingColor(0.35f, 0.35f, 0.35f);
        w = getTextWidth(DEVELOPED_BY, 8);
        content.beginText();
        content.newLineAtOffset((A4_WIDTH - w) / 2, y);
        content.showText(DEVELOPED_BY);
        content.endText();
    }

    private static void drawSimpleFooter(PDPageContentStream content, float y, String extra) throws IOException {
        content.setLineWidth(0.5f);
        content.setStrokingColor(0.35f, 0.35f, 0.35f);
        content.moveTo(MARGIN, y);
        content.lineTo(A4_WIDTH - MARGIN, y);
        content.stroke();

        y -= 18;
        content.setFont(PDType1Font.HELVETICA, 9);
        content.setNonStrokingColor(COLOR_DARK[0], COLOR_DARK[1], COLOR_DARK[2]);
        content.beginText();
        content.newLineAtOffset(MARGIN, y);
        content.showText("Generated: " + LocalDateTime.now().format(DATE_FORMAT));
        content.endText();

        if (extra != null) {
            float w = getTextWidth(extra, 9);
            content.beginText();
            content.newLineAtOffset(A4_WIDTH - MARGIN - w, y);
            content.showText(extra);
            content.endText();
        }

        y -= 15;
        content.setFont(PDType1Font.HELVETICA, 8);
        content.setNonStrokingColor(0.35f, 0.35f, 0.35f);
        float w = getTextWidth(DEVELOPED_BY, 8);
        content.beginText();
        content.newLineAtOffset((A4_WIDTH - w) / 2, y);
        content.showText(DEVELOPED_BY);
        content.endText();
    }

    private static float getTextWidth(String text, int size) {
        return text.length() * size * 0.5f;
    }
}