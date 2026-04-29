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

// Thermal Receipt Constants (80mm width for POS thermal printer)
private static final float THERMAL_WIDTH_MM = 80f;
private static final float THERMAL_WIDTH = THERMAL_WIDTH_MM * MM_TO_POINTS;
private static final float THERMAL_MARGIN_MM = 5f;
private static final float THERMAL_MARGIN = THERMAL_MARGIN_MM * MM_TO_POINTS;
private static final float THERMAL_CONTENT_WIDTH = THERMAL_WIDTH - 2 * THERMAL_MARGIN;

// Thermal Receipt Fonts (Helvetica to match A4 professional style)
private static final PDType1Font THERMAL_FONT = PDType1Font.HELVETICA;
private static final PDType1Font THERMAL_FONT_BOLD = PDType1Font.HELVETICA_BOLD;
    private static final float THERMAL_FONT_7 = 7f;
    private static final float THERMAL_FONT_8 = 8f;
    private static final float THERMAL_FONT_9 = 9f;
    private static final float THERMAL_FONT_10 = 10f;
    private static final String SEPARATOR = "------------------------------------------------";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
    private static final DateTimeFormatter BILL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // A4 Thermal-Style Bill Constants
    private static final float A4_BILL_WIDTH_MM = 100f; // Content width like thermal but on A4
    private static final float A4_BILL_WIDTH = A4_BILL_WIDTH_MM * MM_TO_POINTS;
    private static final float A4_BILL_MARGIN = (A4_WIDTH - A4_BILL_WIDTH) / 2; // Center on A4

    // ================================
    // 1. CUSTOMER REPORT - 80mm (Same Text as A4 Page)
    // ================================
    public static byte[] CustomerReportThermal(List<CustomerEntity> customers) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            float h = 2 * THERMAL_MARGIN + 80 + customers.size() * 14 + 50;
            PDPage page = new PDPage(new PDRectangle(THERMAL_WIDTH, Math.max(h, 350)));
            document.addPage(page);
            PDPageContentStream c = new PDPageContentStream(document, page);

            float y = page.getMediaBox().getHeight() - THERMAL_MARGIN;
            float x = THERMAL_MARGIN;
            float w = THERMAL_WIDTH - 2 * THERMAL_MARGIN;

            // Company Header (matches product thermal PDF)
            c.setFont(PDType1Font.HELVETICA_BOLD, 18);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            float twH = PDType1Font.HELVETICA_BOLD.getStringWidth(COMPANY_NAME) / 1000f * 18f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - twH)/2, y - 18); c.showText(COMPANY_NAME); c.endText();
            y -= 22;
            c.setLineWidth(1f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
            y -= 8;

            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.3f, 0.3f, 0.3f);
            float twA = PDType1Font.HELVETICA.getStringWidth(COMPANY_ADDRESS) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - twA)/2, y - 9); c.showText(COMPANY_ADDRESS); c.endText();
            y -= 12;
            float twP = PDType1Font.HELVETICA.getStringWidth(COMPANY_PHONE) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - twP)/2, y - 9); c.showText(COMPANY_PHONE); c.endText();
            y -= 15;

            // Title (centered, smaller)
            c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_8);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            float tw1 = getThermalTextWidth("CUSTOMER REPORT", THERMAL_FONT_8);
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw1)/2, y - THERMAL_FONT_8); c.showText("CUSTOMER REPORT"); c.endText();
            y -= 16;

            // Total (centered, smaller)
            c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_7);
            String count = "Total: " + customers.size();
            float tw2 = getThermalTextWidth(count, THERMAL_FONT_7);
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw2)/2, y - THERMAL_FONT_7); c.showText(count); c.endText();
            y -= 15;

            c.setLineWidth(1f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
            y -= 20;

            // Table Header
            c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_8);
            c.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
            c.setLineWidth(0f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.setNonStrokingColor(0.2f, 0.2f, 0.4f);
            c.addRect(x, y - 14, w, 14); c.fill();
            c.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
            c.beginText(); c.newLineAtOffset(x + 5, y - 10); c.showText("ID"); c.endText();
            c.beginText(); c.newLineAtOffset(x + 45, y - 10); c.showText("Name"); c.endText();
            c.beginText(); c.newLineAtOffset(x + w - 80, y - 10); c.showText("Contact"); c.endText();
            y -= 14;

            // Table Rows
            c.setFont(THERMAL_FONT, THERMAL_FONT_8);
            int idx = 1;
            for (CustomerEntity customer : customers) {
                if (y < THERMAL_MARGIN + 60) break;
                if (idx % 2 == 0) {
                    c.setNonStrokingColor(0.95f, 0.95f, 1.0f);
                    c.addRect(x, y - 12, w, 12); c.fill();
                }
                c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                String id = customer.getCustId() != null ? customer.getCustId() : "-";
                String name = truncate(customer.getName() != null ? customer.getName() : "-", 14);
                String contact = customer.getContact() != null ? customer.getContact() : "-";
                c.beginText(); c.newLineAtOffset(x + 5, y - 9); c.showText(id); c.endText();
                c.beginText(); c.newLineAtOffset(x + 45, y - 9); c.showText(name); c.endText();
                float cw = getThermalTextWidth(contact, THERMAL_FONT_8);
                c.beginText(); c.newLineAtOffset(x + w - 5 - cw, y - 9); c.showText(contact); c.endText();
                c.setLineWidth(0.2f); c.setStrokingColor(0.8f, 0.8f, 0.8f);
                c.moveTo(x, y - 12); c.lineTo(x + w, y - 12); c.stroke();
                y -= 12;
                idx++;
            }

            c.setLineWidth(1f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
            y -= 20;

            // Footer (matches product thermal PDF)
            c.setLineWidth(0.3f); c.setStrokingColor(0.8f, 0.8f, 0.8f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
            y -= 10;
            c.setFont(THERMAL_FONT, THERMAL_FONT_8);
            c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            float twT = getThermalTextWidth("Thank you for your business!", THERMAL_FONT_8);
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - twT)/2, y - THERMAL_FONT_8); c.showText("Thank you for your business!"); c.endText();
            String gen = "Generated: " + LocalDateTime.now().format(DATE_FORMAT);
            float twG = getThermalTextWidth(gen, THERMAL_FONT_8);
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - twG)/2, y - 20); c.showText(gen); c.endText();

            c.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating A4 customer report", e);
        }
    }

    // ================================
    // 2. CUSTOMER REPORT - A4 (Professional Style)
    // ================================
    public static byte[] CustomerReportA4Pro(List<CustomerEntity> customers) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            float m = 40f;
            float w = A4_WIDTH - 2 * m;
            float rowH = 18;
            float col1 = m + 10, col2 = m + 120, col3 = m + 380;
            float[] vLines = {m, m + 90, m + 360, A4_WIDTH - m};
            float threshold = 150;

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream c = new PDPageContentStream(document, page);
            float y = A4_HEIGHT - m;
            drawReportHeader(c, "CUSTOMER REPORT", y);
            y -= 85;

            // Draw table header
            c.setFont(PDType1Font.HELVETICA_BOLD, 9);
            c.setNonStrokingColor(0.15f, 0.15f, 0.4f);
            c.addRect(m, y - rowH, w, rowH);
            c.fill();
            c.setLineWidth(0.5f);
            c.setStrokingColor(0.5f, 0.5f, 0.5f);
            c.addRect(m, y - rowH, w, rowH);
            c.stroke();
            for (float vl : vLines) {
                c.moveTo(vl, y); c.lineTo(vl, y - rowH); c.stroke();
            }
            c.setNonStrokingColor(1f, 1f, 1f);
            c.beginText(); c.newLineAtOffset(col1, y - 12); c.showText("ID"); c.endText();
            c.beginText(); c.newLineAtOffset(col2, y - 12); c.showText("Name"); c.endText();
            c.beginText(); c.newLineAtOffset(col3, y - 12); c.showText("Contact"); c.endText();
            y -= rowH;

            // Table Rows
            c.setFont(PDType1Font.HELVETICA, 9);
            int idx = 1;
            int pageNum = 1;
            for (CustomerEntity customer : customers) {
                if (y < threshold + rowH) {
                    c.setLineWidth(0.5f);
                    c.setStrokingColor(0.5f, 0.5f, 0.5f);
                    c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
                    drawFooter(c, pageNum);
                    c.close();

                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    c = new PDPageContentStream(document, page);
                    y = A4_HEIGHT - m;
                    drawReportHeader(c, "CUSTOMER REPORT", y);
                    y -= 85;

                    c.setFont(PDType1Font.HELVETICA_BOLD, 9);
                    c.setNonStrokingColor(0.15f, 0.15f, 0.4f);
                    c.addRect(m, y - rowH, w, rowH);
                    c.fill();
                    c.setLineWidth(0.5f);
                    c.setStrokingColor(0.5f, 0.5f, 0.5f);
                    c.addRect(m, y - rowH, w, rowH);
                    c.stroke();
                    for (float vl : vLines) {
                        c.moveTo(vl, y); c.lineTo(vl, y - rowH); c.stroke();
                    }
                    c.setNonStrokingColor(1f, 1f, 1f);
                    c.beginText(); c.newLineAtOffset(col1, y - 12); c.showText("ID"); c.endText();
                    c.beginText(); c.newLineAtOffset(col2, y - 12); c.showText("Name"); c.endText();
                    c.beginText(); c.newLineAtOffset(col3, y - 12); c.showText("Contact"); c.endText();
                    y -= rowH;
                    pageNum++;
                }

                if (idx % 2 == 0) {
                    c.setNonStrokingColor(0.95f, 0.95f, 0.98f);
                    c.addRect(m, y - rowH, w, rowH);
                    c.fill();
                }

                c.setLineWidth(0.3f);
                c.setStrokingColor(0.7f, 0.7f, 0.7f);
                c.addRect(m, y - rowH, w, rowH);
                c.stroke();
                for (float vl : vLines) {
                    c.moveTo(vl, y); c.lineTo(vl, y - rowH); c.stroke();
                }

                c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                String id = customer.getCustId() != null ? customer.getCustId() : "-";
                c.beginText(); c.newLineAtOffset(col1, y - 12); c.showText(id); c.endText();

                String nm = customer.getName() != null ? customer.getName() : "-";
                c.beginText(); c.newLineAtOffset(col2, y - 12); c.showText(nm.length() > 30 ? nm.substring(0, 27) + "..." : nm); c.endText();

                String contact = customer.getContact() != null ? customer.getContact() : "-";
                c.beginText(); c.newLineAtOffset(col3, y - 12); c.showText(contact); c.endText();

                y -= rowH;
                idx++;
            }

            // Bottom border
            c.setLineWidth(0.5f);
            c.setStrokingColor(0.5f, 0.5f, 0.5f);
            c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
            y -= 25;

            // Summary (right aligned)
            float sumX = A4_WIDTH - m - 220;
            float sumW = 220;
            c.setFont(PDType1Font.HELVETICA, 10);
            c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            String totalStr = String.format("Total Customers: %d", customers.size());
            float tw2 = PDType1Font.HELVETICA.getStringWidth(totalStr) / 1000f * 10f;
            c.beginText(); c.newLineAtOffset(sumX + sumW - tw2, y - 10); c.showText(totalStr); c.endText();

            drawFooter(c, pageNum);

            c.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating A4 customer report", e);
        }
    }

    // ================================
    // 3. PRODUCT REPORT - 80mm (Same Text as A4 Page, Centered)
    // ================================
    public static byte[] ProductReportThermal(List<ProductEntity> products) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            float h = 2 * THERMAL_MARGIN + 80 + products.size() * 14 + 50;
            PDPage page = new PDPage(new PDRectangle(THERMAL_WIDTH, Math.max(h, 350)));
            document.addPage(page);
            PDPageContentStream c = new PDPageContentStream(document, page);

            float y = page.getMediaBox().getHeight() - THERMAL_MARGIN;
            float x = THERMAL_MARGIN;
            float w = THERMAL_WIDTH - 2 * THERMAL_MARGIN;

            // Company Header
            c.setFont(PDType1Font.HELVETICA_BOLD, 18);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            float tw1 = PDType1Font.HELVETICA_BOLD.getStringWidth(COMPANY_NAME) / 1000f * 18f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw1)/2, y - 18); c.showText(COMPANY_NAME); c.endText();
            y -= 22;
            c.setLineWidth(1f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
            y -= 8;

            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.3f, 0.3f, 0.3f);
            float tw2 = PDType1Font.HELVETICA.getStringWidth(COMPANY_ADDRESS) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw2)/2, y - 9); c.showText(COMPANY_ADDRESS); c.endText();
            y -= 12;
            float tw3 = PDType1Font.HELVETICA.getStringWidth(COMPANY_PHONE) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw3)/2, y - 9); c.showText(COMPANY_PHONE); c.endText();
            y -= 15;

            // Title (centered)
            c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_8);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            float tw4 = PDType1Font.HELVETICA_BOLD.getStringWidth("PRODUCT REPORT") / 1000f * 8f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw4)/2, y - THERMAL_FONT_8); c.showText("PRODUCT REPORT"); c.endText();
            y -= 16;
            
            // Total (centered)
            c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_7);
            String count = "Total: " + products.size();
            float tw5 = PDType1Font.HELVETICA_BOLD.getStringWidth(count) / 1000f * 7f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw5)/2, y - THERMAL_FONT_7); c.showText(count); c.endText();
            y -= 15;

            y -= 10;
            c.setLineWidth(1f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
            y -= 20;

            // Table Header (3 columns: Name, Price, Qty)
            c.setFont(THERMAL_FONT_BOLD, 8);
            c.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
            c.setLineWidth(0f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            c.addRect(x, y - 16, w, 16); c.fill();
            c.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
            float col1 = x + 10, col2 = x + w * 0.6f, col3 = x + w * 0.8f;
            float col1w = w * 0.5f, col2w = w * 0.2f, col3w = w * 0.2f;
            c.beginText(); c.newLineAtOffset(col1, y - 11); c.showText("Name"); c.endText();
            c.beginText(); c.newLineAtOffset(col2 + (col2w - getThermalTextWidth("Price", THERMAL_FONT_8))/2, y - 11); c.showText("Price"); c.endText();
            c.beginText(); c.newLineAtOffset(col3 + (col3w - getThermalTextWidth("Qty", THERMAL_FONT_8))/2, y - 11); c.showText("Qty"); c.endText();
            y -= 16;

            // Table Rows (3 columns: Name, Price, Qty)
            c.setFont(THERMAL_FONT, THERMAL_FONT_9);
            int idx = 1;
            int lowStock = 0;
            for (ProductEntity p : products) {
                if (y < THERMAL_MARGIN + 60) break;
                if (p.getQty() < 10) lowStock++;
                if (idx % 2 == 0) {
                    c.setNonStrokingColor(0.95f, 0.95f, 1.0f);
                    c.addRect(x, y - 14, w, 14); c.fill();
                }
                c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                String name = truncate(p.getName() != null ? p.getName() : "-", 20);
                String price = String.format("%.2f", p.getPrice());
                String qty = String.valueOf(p.getQty());
                // Left-align Name
                c.beginText(); c.newLineAtOffset(col1, y - 10); c.showText(name); c.endText();
                // Center Price
                float pw = PDType1Font.HELVETICA.getStringWidth(price) / 1000f * 9f;
                c.beginText(); c.newLineAtOffset(col2 + (col2w - pw)/2, y - 10); c.showText(price); c.endText();
                // Center Qty
                float qw = PDType1Font.HELVETICA.getStringWidth(qty) / 1000f * 9f;
                c.beginText(); c.newLineAtOffset(col3 + (col3w - qw)/2, y - 10); c.showText(qty); c.endText();
                c.setLineWidth(0.3f); c.setStrokingColor(0.8f, 0.8f, 0.8f);
                c.moveTo(x, y - 14); c.lineTo(x + w, y - 14); c.stroke();
                y -= 14;
                idx++;
            }

            c.setLineWidth(1f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
            y -= 15;

            // Low Stock (centered)
            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            String lowStr = String.format("Low Stock Items: %d", lowStock);
            float tw6 = PDType1Font.HELVETICA.getStringWidth(lowStr) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw6)/2, y - 9); c.showText(lowStr); c.endText();
            y -= 20;

            // Footer
            c.setLineWidth(0.3f); c.setStrokingColor(0.8f, 0.8f, 0.8f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
            y -= 10;
            y -= 1; // Bottom margin
            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            float tw7 = getThermalTextWidth("Thank you for your business!", THERMAL_FONT_8);
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw7)/2, y - THERMAL_FONT_8); c.showText("Thank you for your business!"); c.endText();
            y -= 20;
            String gen = "Generated: " + LocalDateTime.now().format(DATE_FORMAT);
            float tw8 = getThermalTextWidth(gen, THERMAL_FONT_8);
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw8)/2, y - 9); c.showText(gen); c.endText();

            c.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating A4 product report", e);
        }
    }

    // ================================
    // Helper Methods
    // ================================

    private static void drawFooter(PDPageContentStream c, int pageNum) throws IOException {
        float footerY = 40f;
        c.setLineWidth(1f);
        c.setStrokingColor(0.8f, 0.8f, 0.8f);
        c.moveTo(40, footerY); c.lineTo(A4_WIDTH - 40, footerY); c.stroke();
        c.setFont(PDType1Font.HELVETICA, 9);
        c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
        c.beginText(); c.newLineAtOffset(40, footerY - 12); c.showText("Thank you for using our system!"); c.endText();
        String pg = String.format("Page %d", pageNum);
        float pgW = PDType1Font.HELVETICA.getStringWidth(pg) / 1000f * 9f;
        c.beginText(); c.newLineAtOffset(A4_WIDTH - 40 - pgW, footerY - 12); c.showText(pg); c.endText();
    }

    private static void drawReportHeader(PDPageContentStream c, String reportTitle, float startY) throws IOException {
        c.setFont(PDType1Font.HELVETICA_BOLD, 16);
        c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
        c.beginText(); c.newLineAtOffset(40, startY - 16); c.showText(COMPANY_NAME); c.endText();

        c.setFont(PDType1Font.HELVETICA, 9);
        c.setNonStrokingColor(0.3f, 0.3f, 0.3f);
        c.beginText(); c.newLineAtOffset(40, startY - 30); c.showText(COMPANY_ADDRESS); c.endText();
        c.beginText(); c.newLineAtOffset(40, startY - 42); c.showText(COMPANY_PHONE); c.endText();

        float tw = PDType1Font.HELVETICA_BOLD.getStringWidth(reportTitle) / 1000f * 28f;
        c.setFont(PDType1Font.HELVETICA_BOLD, 28);
        c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
        c.beginText(); c.newLineAtOffset(A4_WIDTH - 40 - tw, startY - 28); c.showText(reportTitle); c.endText();

        c.setLineWidth(2f);
        c.setStrokingColor(0.1f, 0.1f, 0.3f);
        c.moveTo(40, startY - 65); c.lineTo(A4_WIDTH - 40, startY - 65); c.stroke();
    }

    private static PDPageContentStream newPageWithFooter(PDDocument document, PDPageContentStream prevC, String reportTitle, int pageNum) throws IOException {
        prevC.close();
        drawFooter(prevC, pageNum);
        PDPage newPage = new PDPage(PDRectangle.A4);
        document.addPage(newPage);
        PDPageContentStream c = new PDPageContentStream(document, newPage);
        drawReportHeader(c, reportTitle, A4_HEIGHT - 40);
        return c;
    }

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
    // 3. THERMAL ORDER BILL (80mm width - Professional Real-World Style)
    // ================================
    public static byte[] generateOrderBill(OrderEntity order, List<OrderProductDetailsEntity> details) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            float pageHeight = calculateThermalHeight(details);
            PDPage page = new PDPage(new PDRectangle(THERMAL_WIDTH, pageHeight));
            document.addPage(page);
            PDPageContentStream c = new PDPageContentStream(document, page);

            float y = pageHeight - THERMAL_MARGIN;
            float x = THERMAL_MARGIN;
            float w = THERMAL_WIDTH - 2 * THERMAL_MARGIN;

            // Header with box
            y = drawThermalHeaderPro(c, x, w, y);

            // Invoice title
            y = drawThermalTitlePro(c, x, w, y);

            // Bill details
            y = drawThermalDetailsPro(c, x, w, y, order);

            // Items table with borders
            y = drawThermalTablePro(c, x, w, y, details);

            // Summary with lines (now passes order to use actual tax)
            y = drawThermalSummaryPro(c, x, w, y, order, details);

            // Payment info
            y = drawThermalPaymentPro(c, x, w, y, order, details);

            // Footer
            drawThermalFooterPro(c, x, w, y);

            c.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating thermal order bill", e);
        }
    }

    private static float calculateThermalHeight(List<OrderProductDetailsEntity> details) {
        float h = 2 * THERMAL_MARGIN;
        h += 35; // Header with box
        h += 25; // Title
        h += 45; // Details
        h += 20 + details.size() * 16; // Table with borders
        h += 70; // Summary with lines
        h += 40; // Payment
        h += 50; // Footer with signature
        return Math.max(h, 350);
    }

    private static float drawThermalHeaderPro(PDPageContentStream c, float x, float w, float y) throws IOException {
        // Company name - bold and centered (larger font)
        c.setFont(THERMAL_FONT_BOLD, 14);
        c.setNonStrokingColor(0f, 0f, 0f);
        float tw = getThermalTextWidth(COMPANY_NAME, 14);
        c.beginText(); c.newLineAtOffset(x + (w - tw)/2, y - 14); c.showText(COMPANY_NAME); c.endText();
        y -= 22;

        // Tagline
        c.setFont(THERMAL_FONT, 7);
        tw = getThermalTextWidth(COMPANY_TAGLINE, 7);
        c.beginText(); c.newLineAtOffset(x + (w - tw)/2, y - 7); c.showText(COMPANY_TAGLINE); c.endText();
        y -= 12;

        // Address
        c.setFont(THERMAL_FONT, 7);
        tw = getThermalTextWidth(COMPANY_ADDRESS, 7);
        c.beginText(); c.newLineAtOffset(x + (w - tw)/2, y - 7); c.showText(COMPANY_ADDRESS); c.endText();
        y -= 10;

        // Phone
        tw = getThermalTextWidth(COMPANY_PHONE, 7);
        c.beginText(); c.newLineAtOffset(x + (w - tw)/2, y - 7); c.showText(COMPANY_PHONE); c.endText();
        y -= 15;

        // Separator line
        c.setLineWidth(0.5f);
        c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
        y -= 10;

        return y;
    }

    private static float drawThermalTitlePro(PDPageContentStream c, float x, float w, float y) throws IOException {
        // Title with background
        c.setFont(THERMAL_FONT_BOLD, 11);
        String title = "INVOICE / CASH MEMO";
        float tw = getThermalTextWidth(title, 11);
        float titleX = x + (w - tw)/2;

        // Draw background rectangle
        c.setNonStrokingColor(0.95f, 0.95f, 0.95f);
        c.addRect(x, y - 16, w, 16);
        c.fill();

        // Draw title text
        c.setNonStrokingColor(0f, 0f, 0f);
        c.beginText(); c.newLineAtOffset(titleX, y - 11); c.showText(title); c.endText();
        y -= 20;

        // Line under title
        c.setLineWidth(0.5f);
        c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
        y -= 8;

        return y;
    }

    private static float drawThermalDetailsPro(PDPageContentStream c, float x, float w, float y, OrderEntity order) throws IOException {
        c.setFont(THERMAL_FONT, 8);
        c.setNonStrokingColor(0f, 0f, 0f);

        // Left side: Bill info
        c.beginText(); c.newLineAtOffset(x, y - 8); c.showText("Bill No: " + order.getOrderId()); c.endText();
        y -= 12;

        String dateStr = order.getDate() != null ? order.getDate().format(BILL_DATE_FORMAT) : "-";
        c.beginText(); c.newLineAtOffset(x, y - 8); c.showText("Date: " + dateStr); c.endText();
        y -= 12;

        // Right side: Customer info
        float rightX = x + w * 0.5f;
        y += 24; // Go back up to align with left side

        String custName = order.getCustomer() != null ? order.getCustomer().getName() : "-";
        String custText = "Customer: " + custName;
        float tw = getThermalTextWidth(custText, 8);
        c.beginText(); c.newLineAtOffset(x + w - tw, y - 8); c.showText(custText); c.endText();
        y -= 12;

        if (order.getCustomer() != null && order.getCustomer().getContact() != null) {
            String contactText = "Contact: " + order.getCustomer().getContact();
            tw = getThermalTextWidth(contactText, 8);
            c.beginText(); c.newLineAtOffset(x + w - tw, y - 8); c.showText(contactText); c.endText();
        }
        y -= 16;

        // Separator
        c.setLineWidth(0.5f);
        c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
        y -= 8;

        return y;
    }

    private static float drawThermalTablePro(PDPageContentStream c, float x, float w, float y, List<OrderProductDetailsEntity> details) throws IOException {
        // Table header with background
        c.setFont(THERMAL_FONT_BOLD, 8);
        c.setNonStrokingColor(0f, 0f, 0f);
        c.setLineWidth(0.5f);

        // Header background
        c.setNonStrokingColor(0.85f, 0.85f, 0.85f);
        c.addRect(x, y - 14, w, 14);
        c.fill();

        // Header border
        c.setStrokingColor(0f, 0f, 0f);
        c.addRect(x, y - 14, w, 14);
        c.stroke();

        // Header text
        c.setNonStrokingColor(0f, 0f, 0f);
        float colItem = x + 2, colQty = x + w * 0.5f, colRate = x + w * 0.7f, colAmt = x + w * 0.85f;

        c.beginText(); c.newLineAtOffset(colItem, y - 10); c.showText("#"); c.endText();
        c.beginText(); c.newLineAtOffset(colItem + 15, y - 10); c.showText("Item"); c.endText();
        c.beginText(); c.newLineAtOffset(colQty + 5, y - 10); c.showText("Qty"); c.endText();
        c.beginText(); c.newLineAtOffset(colRate + 5, y - 10); c.showText("Rate"); c.endText();
        c.beginText(); c.newLineAtOffset(colAmt + 5, y - 10); c.showText("Amt"); c.endText();
        y -= 14;

        // Table rows
        c.setFont(THERMAL_FONT, 7);
        int idx = 1;
        for (OrderProductDetailsEntity d : details) {
            if (y < THERMAL_MARGIN + 60) break;

            // Alternating row background
            if (idx % 2 == 0) {
                c.setNonStrokingColor(0.97f, 0.97f, 0.97f);
                c.addRect(x, y - 14, w, 14);
                c.fill();
            }

            // Row border
            c.setLineWidth(0.2f);
            c.setStrokingColor(0.7f, 0.7f, 0.7f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();

            String nm = d.getProduct() != null ? truncate(d.getProduct().getName(), 12) : "-";
            String qty = String.valueOf(d.getQuantity());
            String rate = String.format("%.2f", d.getUnitPrice());
            String amt = String.format("%.2f", d.getQuantity() * d.getUnitPrice());

            c.setNonStrokingColor(0f, 0f, 0f);
            c.beginText(); c.newLineAtOffset(colItem, y - 10); c.showText(String.valueOf(idx)); c.endText();
            c.beginText(); c.newLineAtOffset(colItem + 15, y - 10); c.showText(nm); c.endText();
            c.beginText(); c.newLineAtOffset(colQty + 10, y - 10); c.showText(qty); c.endText();
            c.beginText(); c.newLineAtOffset(colRate + 8, y - 10); c.showText(rate); c.endText();
            c.beginText(); c.newLineAtOffset(colAmt + 8, y - 10); c.showText(amt); c.endText();

            y -= 14;
            idx++;
        }

        // Bottom border
        c.setLineWidth(0.5f);
        c.setStrokingColor(0f, 0f, 0f);
        c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
        y -= 8;

        return y;
    }

    // ================================
    // 4. PRODUCT REPORT - A4 (Professional Style)
    // ================================
    public static byte[] ProductReportA4Pro(List<ProductEntity> products) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            float m = 40f;
            float w = A4_WIDTH - 2 * m;
            float rowH = 18;
            float col1 = m + 10, col2 = m + 100, col3 = m + 380, col4 = m + 480;
            float[] vLines = {m, m + 80, m + 360, m + 470, A4_WIDTH - m};
            float threshold = 150;

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream c = new PDPageContentStream(document, page);
            float y = A4_HEIGHT - m;
            drawReportHeader(c, "PRODUCT REPORT", y);
            y -= 85;

            // Draw table header
            c.setFont(PDType1Font.HELVETICA_BOLD, 9);
            c.setNonStrokingColor(0.15f, 0.15f, 0.4f);
            c.addRect(m, y - rowH, w, rowH);
            c.fill();
            c.setLineWidth(0.5f);
            c.setStrokingColor(0.5f, 0.5f, 0.5f);
            c.addRect(m, y - rowH, w, rowH);
            c.stroke();
            for (float vl : vLines) {
                c.moveTo(vl, y); c.lineTo(vl, y - rowH); c.stroke();
            }
            c.setNonStrokingColor(1f, 1f, 1f);
            c.beginText(); c.newLineAtOffset(col1, y - 12); c.showText("ID"); c.endText();
            c.beginText(); c.newLineAtOffset(col2, y - 12); c.showText("Name"); c.endText();
            c.beginText(); c.newLineAtOffset(col3, y - 12); c.showText("Price (LKR)"); c.endText();
            c.beginText(); c.newLineAtOffset(col4, y - 12); c.showText("Qty"); c.endText();
            y -= rowH;

            // Table Rows
            c.setFont(PDType1Font.HELVETICA, 9);
            int idx = 1;
            int pageNum = 1;
            for (ProductEntity p : products) {
                if (y < threshold + rowH) {
                    c.setLineWidth(0.5f);
                    c.setStrokingColor(0.5f, 0.5f, 0.5f);
                    c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
                    drawFooter(c, pageNum);
                    c.close();

                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    c = new PDPageContentStream(document, page);
                    y = A4_HEIGHT - m;
                    drawReportHeader(c, "PRODUCT REPORT", y);
                    y -= 85;

                    c.setFont(PDType1Font.HELVETICA_BOLD, 9);
                    c.setNonStrokingColor(0.15f, 0.15f, 0.4f);
                    c.addRect(m, y - rowH, w, rowH);
                    c.fill();
                    c.setLineWidth(0.5f);
                    c.setStrokingColor(0.5f, 0.5f, 0.5f);
                    c.addRect(m, y - rowH, w, rowH);
                    c.stroke();
                    for (float vl : vLines) {
                        c.moveTo(vl, y); c.lineTo(vl, y - rowH); c.stroke();
                    }
                    c.setNonStrokingColor(1f, 1f, 1f);
                    c.beginText(); c.newLineAtOffset(col1, y - 12); c.showText("ID"); c.endText();
                    c.beginText(); c.newLineAtOffset(col2, y - 12); c.showText("Name"); c.endText();
                    c.beginText(); c.newLineAtOffset(col3, y - 12); c.showText("Price (LKR)"); c.endText();
                    c.beginText(); c.newLineAtOffset(col4, y - 12); c.showText("Qty"); c.endText();
                    y -= rowH;
                    pageNum++;
                }

                if (idx % 2 == 0) {
                    c.setNonStrokingColor(0.95f, 0.95f, 0.98f);
                    c.addRect(m, y - rowH, w, rowH);
                    c.fill();
                }

                c.setLineWidth(0.3f);
                c.setStrokingColor(0.7f, 0.7f, 0.7f);
                c.addRect(m, y - rowH, w, rowH);
                c.stroke();
                for (float vl : vLines) {
                    c.moveTo(vl, y); c.lineTo(vl, y - rowH); c.stroke();
                }

                c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                c.beginText(); c.newLineAtOffset(col1, y - 12); c.showText(p.getId()); c.endText();

                String nm = p.getName() != null ? p.getName() : "-";
                c.beginText(); c.newLineAtOffset(col2, y - 12); c.showText(nm.length() > 30 ? nm.substring(0, 27) + "..." : nm); c.endText();

                String price = String.format("%.2f", p.getPrice());
                c.beginText(); c.newLineAtOffset(col3 + 30, y - 12); c.showText(price); c.endText();

                String qty = String.valueOf(p.getQty());
                c.beginText(); c.newLineAtOffset(col4 + 20, y - 12); c.showText(qty); c.endText();

                y -= rowH;
                idx++;
            }

            // Bottom border
            c.setLineWidth(0.5f);
            c.setStrokingColor(0.5f, 0.5f, 0.5f);
            c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
            y -= 25;

            // Summary
            float sumX = A4_WIDTH - m - 220;
            float sumW = 220;
            c.setFont(PDType1Font.HELVETICA, 10);
            c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            String totalStr = String.format("Total Products: %d", products.size());
            float tw2 = PDType1Font.HELVETICA.getStringWidth(totalStr) / 1000f * 10f;
            c.beginText(); c.newLineAtOffset(sumX + sumW - tw2, y - 10); c.showText(totalStr); c.endText();

            drawFooter(c, pageNum);

            c.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating A4 product report", e);
        }
    }

    private static float drawThermalSummaryPro(PDPageContentStream c, float x, float w, float y, OrderEntity order, List<OrderProductDetailsEntity> details) throws IOException {
        double sub = 0;
        for (OrderProductDetailsEntity d : details) sub += d.getQuantity() * d.getUnitPrice();
        double discountPercent = order.getDiscount(); // Get discount PERCENTAGE from order (e.g., 2.0 for 2%)
        double discount = sub * discountPercent / 100.0; // Calculate discount amount: subTotal * percent / 100
        double totalAfterDiscount = sub - discount;
        double taxRate = order.getTaxRate(); // Get tax RATE from order (e.g., 0.5 for 0.5%)
        double tax = totalAfterDiscount * taxRate / 100.0; // Calculate tax amount: discounted * rate / 100
        double net = totalAfterDiscount + tax;

        c.setFont(THERMAL_FONT, 8);
        c.setNonStrokingColor(0f, 0f, 0f);

        // Subtotal with right margin
        String s1 = String.format("Sub Total: LKR %.2f", sub);
        float tw = getThermalTextWidth(s1, 8);
        c.beginText(); c.newLineAtOffset(x + w - tw - 5, y - 8); c.showText(s1); c.endText();
        y -= 12;

        // Discount with right margin (show percentage and amount)
        String sDiscount = String.format("Discount (%.0f%%): LKR %.2f", discountPercent, discount);
        tw = getThermalTextWidth(sDiscount, 8);
        c.beginText(); c.newLineAtOffset(x + w - tw - 5, y - 8); c.showText(sDiscount); c.endText();
        y -= 12;

        // Tax with right margin (use saved tax rate from order - show exact value)
        String s2 = String.format("Tax (%.1f%%): LKR %.2f", taxRate, tax);
        tw = getThermalTextWidth(s2, 8);
        c.beginText(); c.newLineAtOffset(x + w - tw - 5, y - 8); c.showText(s2); c.endText();
        y -= 12;

        // Line separator
        c.setLineWidth(0.5f);
        c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
        y -= 8;

        // Total - bold with background and right margin
        c.setFont(THERMAL_FONT_BOLD, 10);
        String s3 = String.format("TOTAL: LKR %.2f", net);
        tw = getThermalTextWidth(s3, 10);

        // Total background
        c.setNonStrokingColor(0.9f, 0.9f, 0.9f);
        c.addRect(x, y - 14, w, 14);
        c.fill();

        // Total border
        c.setLineWidth(0.5f);
        c.setStrokingColor(0f, 0f, 0f);
        c.addRect(x, y - 14, w, 14);
        c.stroke();

        // Total text with right margin
        c.setNonStrokingColor(0f, 0f, 0f);
        c.beginText(); c.newLineAtOffset(x + w - tw - 5, y - 10); c.showText(s3); c.endText();
        y -= 18;

        return y;
    }

    private static float drawThermalPaymentPro(PDPageContentStream c, float x, float w, float y, OrderEntity order, List<OrderProductDetailsEntity> details) throws IOException {
        c.setFont(THERMAL_FONT, 8);
        c.setNonStrokingColor(0f, 0f, 0f);

        // Payment info box (border around payment section)
        c.setLineWidth(0.5f);
        c.setStrokingColor(0f, 0f, 0f);
        c.addRect(x, y - 34, w, 34);
        c.stroke();

        // Payment method (left) and Paid amount (right) on same line
        c.beginText(); c.newLineAtOffset(x + 5, y - 12); c.showText("PAYMENT: CASH"); c.endText();

        double paid = order.getAmountPaid(); // Use actual paid amount from order
        String s1 = String.format("Paid: LKR %.2f", paid);
        float tw = getThermalTextWidth(s1, 8);
        c.beginText(); c.newLineAtOffset(x + w - tw - 5, y - 12); c.showText(s1); c.endText();
        y -= 14;

        // Balance on next line (LEFT aligned)
        double balance = order.getBalance(); // Use actual balance from order
        String s2 = String.format("Balance: LKR %.2f", balance);
        c.beginText(); c.newLineAtOffset(x + 5, y - 8); c.showText(s2); c.endText();
        y -= 20;

        return y;
    }

    private static float drawThermalFooterPro(PDPageContentStream c, float x, float w, float y) throws IOException {
        // Separator
        c.setLineWidth(0.5f);
        c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
        y -= 10;

        // Date and time
        c.setFont(THERMAL_FONT, 7);
        c.setNonStrokingColor(0.4f, 0.4f, 0.4f);
        String dt = LocalDateTime.now().format(DATE_FORMAT);
        float tw = getThermalTextWidth(dt, 7);
        c.beginText(); c.newLineAtOffset(x + (w - tw)/2, y - 7); c.showText(dt); c.endText();
        y -= 12;

        // Thank you message
        c.setFont(THERMAL_FONT_BOLD, 9);
        c.setNonStrokingColor(0f, 0f, 0f);
        String ty = "*** Thank You! Visit Again ***";
        tw = getThermalTextWidth(ty, 9);
        c.beginText(); c.newLineAtOffset(x + (w - tw)/2, y - 9); c.showText(ty); c.endText();
        y -= 15;

        // Signature line
        c.setLineWidth(0.3f);
        c.setStrokingColor(0f, 0f, 0f);
        float lineW = 50f;
        float lineX = x + (w - lineW)/2;
        c.moveTo(lineX, y); c.lineTo(lineX + lineW, y); c.stroke();
        y -= 10;

        c.setFont(THERMAL_FONT, 6);
        c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
        String sig = "Authorized Signature";
        tw = getThermalTextWidth(sig, 6);
        c.beginText(); c.newLineAtOffset(x + (w - tw)/2, y - 6); c.showText(sig); c.endText();

        return y;
    }

    private static float getThermalTextWidth(String text, float fontSize) throws IOException {
        if (text == null) return 0;
        return (THERMAL_FONT.getStringWidth(text) / 1000f) * fontSize;
    }

    // ================================
    // 4. A4 THERMAL-STYLE ORDER BILL (same layout as 80mm, centered on A4 - Improved)
    // ================================
    public static byte[] generateOrderBillA4(OrderEntity order, List<OrderProductDetailsEntity> details) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            float pageHeight = calculateThermalHeight(details) + 50;
            PDPage page = new PDPage(new PDRectangle(A4_WIDTH, pageHeight));
            document.addPage(page);
            PDPageContentStream c = new PDPageContentStream(document, page);

            float xOffset = A4_BILL_MARGIN;
            float y = pageHeight - 25 * MM_TO_POINTS;

            // Use the same improved thermal functions but with A4 centering
            y = drawThermalHeaderPro(c, xOffset, A4_BILL_WIDTH, y);
            y = drawThermalTitlePro(c, xOffset, A4_BILL_WIDTH, y);
            y = drawThermalDetailsPro(c, xOffset, A4_BILL_WIDTH, y, order);
            y = drawThermalTablePro(c, xOffset, A4_BILL_WIDTH, y, details);
            y = drawThermalSummaryPro(c, xOffset, A4_BILL_WIDTH, y, order, details);
            y = drawThermalPaymentPro(c, xOffset, A4_BILL_WIDTH, y, order, details);
            drawThermalFooterPro(c, xOffset, A4_BILL_WIDTH, y);

            c.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating A4 thermal-style order bill", e);
        }
    }

    // ================================
    // 5. PROFESSIONAL A4 INVOICE (Real-world layout)
    // ================================
    public static byte[] generateOrderBillA4Pro(OrderEntity order, List<OrderProductDetailsEntity> details) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream c = new PDPageContentStream(document, page);

            float m = 40f;
            float w = A4_WIDTH - 2 * m;
            float y = A4_HEIGHT - m;

            // LEFT SIDE: Company info (Dilshan Oil Mill, Address, Contact)
            c.setFont(PDType1Font.HELVETICA_BOLD, 14);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            c.beginText(); c.newLineAtOffset(m, y - 14); c.showText(COMPANY_NAME); c.endText();

            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.3f, 0.3f, 0.3f);
            c.beginText(); c.newLineAtOffset(m, y - 26); c.showText(COMPANY_ADDRESS); c.endText();
            c.beginText(); c.newLineAtOffset(m, y - 37); c.showText(COMPANY_PHONE); c.endText();

            // RIGHT SIDE: INVOICE
            c.setFont(PDType1Font.HELVETICA_BOLD, 28);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            String title = "INVOICE";
            float titleW = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000f * 28f;
            c.beginText(); c.newLineAtOffset(A4_WIDTH - m - titleW, y - 28); c.showText(title); c.endText();

            y -= 45;

            // Divider line
            c.setLineWidth(2f);
            c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
            y -= 25;

            // LEFT SIDE: Order ID & Date
            c.setFont(PDType1Font.HELVETICA_BOLD, 9);
            c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            c.beginText(); c.newLineAtOffset(m, y - 9); c.showText("ORDER ID"); c.endText();
            c.setFont(PDType1Font.HELVETICA, 10);
            c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            c.beginText(); c.newLineAtOffset(m, y - 23); c.showText(order.getOrderId()); c.endText();

            String orderDate = order.getDate().format(BILL_DATE_FORMAT);
            c.setFont(PDType1Font.HELVETICA_BOLD, 9);
            c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            c.beginText(); c.newLineAtOffset(m, y - 40); c.showText("DATE"); c.endText();
            c.setFont(PDType1Font.HELVETICA, 10);
            c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            c.beginText(); c.newLineAtOffset(m, y - 54); c.showText(orderDate); c.endText();

            // RIGHT SIDE: Bill To with customer info box
            float billBoxW = 240f;
            float billBoxH = 65f;
            float billBoxX = A4_WIDTH - m - billBoxW;
            float billBoxY = y - 5;

            // Box background & border
            c.setNonStrokingColor(0.96f, 0.96f, 0.98f);
            c.addRect(billBoxX, billBoxY - billBoxH, billBoxW, billBoxH);
            c.fill();
            c.setLineWidth(0.5f);
            c.setStrokingColor(0.7f, 0.7f, 0.7f);
            c.addRect(billBoxX, billBoxY - billBoxH, billBoxW, billBoxH);
            c.stroke();

            // Bill To label
            c.setFont(PDType1Font.HELVETICA_BOLD, 9);
            c.setNonStrokingColor(0.4f, 0.4f, 0.4f);
            c.beginText(); c.newLineAtOffset(billBoxX + 10, billBoxY - 14); c.showText("BILL TO"); c.endText();

            // Customer details
            c.setFont(PDType1Font.HELVETICA_BOLD, 10);
            c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            String custName = order.getCustomer() != null ? order.getCustomer().getName() : "-";
            c.beginText(); c.newLineAtOffset(billBoxX + 10, billBoxY - 30); c.showText(custName); c.endText();

            if (order.getCustomer() != null && order.getCustomer().getContact() != null) {
                c.setFont(PDType1Font.HELVETICA, 9);
                c.beginText(); c.newLineAtOffset(billBoxX + 10, billBoxY - 44); c.showText("Contact: " + order.getCustomer().getContact()); c.endText();
            }

            y -= 75;

            // Table with proper borders
            float tableY = y;
            float rowH = 18;

            // Table header with dark background
            c.setFont(PDType1Font.HELVETICA_BOLD, 9);
            c.setNonStrokingColor(0.15f, 0.15f, 0.4f);
            c.addRect(m, tableY - rowH, w, rowH);
            c.fill();

            // Header border
            c.setLineWidth(1f);
            c.setStrokingColor(0f, 0f, 0f);
            c.addRect(m, tableY - rowH, w, rowH);
            c.stroke();

            // Header text
            c.setNonStrokingColor(1f, 1f, 1f);
            float col1 = m + 10, col2 = m + 50, col3 = m + 280, col4 = m + 340, col5 = m + 430;
            c.beginText(); c.newLineAtOffset(col1, tableY - 12); c.showText("#"); c.endText();
            c.beginText(); c.newLineAtOffset(col2, tableY - 12); c.showText("Description"); c.endText();
            c.beginText(); c.newLineAtOffset(col3 + 10, tableY - 12); c.showText("Qty"); c.endText();
            c.beginText(); c.newLineAtOffset(col4 + 15, tableY - 12); c.showText("Rate (LKR)"); c.endText();
            c.beginText(); c.newLineAtOffset(col5 + 20, tableY - 12); c.showText("Amount (LKR)"); c.endText();

            y = tableY - rowH;

            // Table Rows
            c.setFont(PDType1Font.HELVETICA, 9);
            int idx = 1;
            double grandTotal = 0;
            for (OrderProductDetailsEntity d : details) {
                if (y < 200) break;
                double lineTotal = d.getQuantity() * d.getUnitPrice();
                grandTotal += lineTotal;

                // Alternating row background
                if (idx % 2 == 0) {
                    c.setNonStrokingColor(0.95f, 0.95f, 0.98f);
                    c.addRect(m, y - rowH, w, rowH);
                    c.fill();
                }

                // Row border
                c.setLineWidth(0.3f);
                c.setStrokingColor(0.7f, 0.7f, 0.7f);
                c.addRect(m, y - rowH, w, rowH);
                c.stroke();

                // Row text
                c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                String nm = d.getProduct() != null ? d.getProduct().getName() : "-";
                c.beginText(); c.newLineAtOffset(col1, y - 12); c.showText(String.valueOf(idx)); c.endText();
                c.beginText(); c.newLineAtOffset(col2, y - 12); c.showText(truncate(nm, 30)); c.endText();
                c.beginText(); c.newLineAtOffset(col3 + 15, y - 12); c.showText(String.valueOf(d.getQuantity())); c.endText();
                String rate = String.format("%.2f", d.getUnitPrice());
                c.beginText(); c.newLineAtOffset(col4 + 20, y - 12); c.showText(rate); c.endText();
                String amt = String.format("%.2f", lineTotal);
                c.beginText(); c.newLineAtOffset(col5 + 25, y - 12); c.showText(amt); c.endText();

                y -= rowH;
                idx++;
            }

            // Bottom border
            c.setLineWidth(1f);
            c.setStrokingColor(0.15f, 0.15f, 0.4f);
            c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
            y -= 15;

            // Payment info box (LEFT side)
            c.setLineWidth(0.5f);
            c.setStrokingColor(0.7f, 0.7f, 0.7f);
            c.addRect(m, y - 30, 200, 30);
            c.stroke();

            c.setFont(PDType1Font.HELVETICA_BOLD, 9);
            c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            c.beginText(); c.newLineAtOffset(m + 10, y - 15); c.showText("PAYMENT METHOD: CASH"); c.endText();

            // Summary section (RIGHT aligned)
            float sumX = A4_WIDTH - m - 220;
            float sumW = 220;

            c.setFont(PDType1Font.HELVETICA, 10);
            c.setNonStrokingColor(0.1f, 0.1f, 0.1f);

            // Subtotal
            String subStr = String.format("Sub Total: LKR %,.2f", grandTotal);
            float tw1 = PDType1Font.HELVETICA.getStringWidth(subStr) / 1000f * 10f;
            c.beginText(); c.newLineAtOffset(sumX + sumW - tw1, y - 10); c.showText(subStr); c.endText();
            y -= 30;

            // Discount (calculate amount from percentage)
            double discountPercent = order.getDiscount(); // Get discount PERCENTAGE (e.g., 2.0 for 2%)
            double discount = grandTotal * discountPercent / 100.0; // Calculate: subTotal * percent / 100
            String discountStr = String.format("Discount (%.0f%%): LKR %,.2f", discountPercent, discount);
            float tw2 = PDType1Font.HELVETICA.getStringWidth(discountStr) / 1000f * 10f;
            c.beginText(); c.newLineAtOffset(sumX + sumW - tw2, y - 10); c.showText(discountStr); c.endText();
            y -= 18;

            // Tax - calculate amount from rate on discounted amount
            double totalAfterDiscount = grandTotal - discount;
            double taxRate = order.getTaxRate(); // Get tax RATE (e.g., 0.5)
            double tax = totalAfterDiscount * taxRate / 100.0; // Calculate TAX on discounted amount
            String taxStr = String.format("Tax (%.1f%%): LKR %,.2f", taxRate, tax);
            float tw3 = PDType1Font.HELVETICA.getStringWidth(taxStr) / 1000f * 10f;
            c.beginText(); c.newLineAtOffset(sumX + sumW - tw3, y - 10); c.showText(taxStr); c.endText();
            y -= 35;

            // Separator
            c.setLineWidth(1f);
            c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(sumX, y + 8); c.lineTo(sumX + sumW, y + 8); c.stroke();
            y -= 12;

            // Total with background
            c.setFont(PDType1Font.HELVETICA_BOLD, 12);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            double net = totalAfterDiscount + tax; // totalAfterDiscount already calculated
            String totalStr = String.format("TOTAL: LKR %,.2f", net);
            float tw4 = PDType1Font.HELVETICA_BOLD.getStringWidth(totalStr) / 1000f * 12f;

            // Total background
            c.setNonStrokingColor(0.9f, 0.95f, 1f);
            c.addRect(sumX, y - 18, sumW, 18);
            c.fill();

            // Total border
            c.setLineWidth(0.5f);
            c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.addRect(sumX, y - 18, sumW, 18);
            c.stroke();

            // Total text
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            c.beginText(); c.newLineAtOffset(sumX + sumW - tw4 - 5, y - 13); c.showText(totalStr); c.endText();
            y -= 50;

            // Footer
            c.setLineWidth(1f);
            c.setStrokingColor(0.8f, 0.8f, 0.8f);
            c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
            y -= 15;

            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            c.beginText(); c.newLineAtOffset(m, y - 9); c.showText("Thank you for your business!"); c.endText();

            String gen = "Generated: " + LocalDateTime.now().format(DATE_FORMAT);
            float tw = PDType1Font.HELVETICA.getStringWidth(gen) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset(A4_WIDTH - m - tw, y - 9); c.showText(gen); c.endText();

            // Signature line
            y -= 30;
            c.setLineWidth(0.5f);
            c.setStrokingColor(0f, 0f, 0f);
            float sigLineW = 150f;
            float sigX = A4_WIDTH - m - sigLineW;
            c.moveTo(sigX, y); c.lineTo(sigX + sigLineW, y); c.stroke();
            y -= 12;
            c.setFont(PDType1Font.HELVETICA, 8);
            c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            String sig = "Authorized Signature";
            float tw6 = PDType1Font.HELVETICA.getStringWidth(sig) / 1000f * 8f;
            c.beginText(); c.newLineAtOffset(sigX + (sigLineW - tw6)/2, y - 8); c.showText(sig); c.endText();

            c.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating A4 invoice", e);
        }
    }
}