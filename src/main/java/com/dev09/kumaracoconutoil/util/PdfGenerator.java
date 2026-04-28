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
    private static final DateTimeFormatter BILL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

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

            // Company Header
            c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_10);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            float tw = getThermalTextWidth(COMPANY_NAME, THERMAL_FONT_10);
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - THERMAL_FONT_10); c.showText(COMPANY_NAME); c.endText();
            y -= 14;
            c.setLineWidth(1f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
            y -= 8;

            c.setFont(THERMAL_FONT, THERMAL_FONT_9);
            c.setNonStrokingColor(0.3f, 0.3f, 0.3f);
            tw = getThermalTextWidth(COMPANY_ADDRESS, THERMAL_FONT_9);
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - THERMAL_FONT_9); c.showText(COMPANY_ADDRESS); c.endText();
            y -= 12;
            tw = getThermalTextWidth(COMPANY_PHONE, THERMAL_FONT_9);
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - THERMAL_FONT_9); c.showText(COMPANY_PHONE); c.endText();
            y -= 15;

            // Title (right side like A4)
            c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_10);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            tw = getThermalTextWidth("CUSTOMER REPORT", THERMAL_FONT_10);
            c.beginText(); c.newLineAtOffset(x + w - tw, y - THERMAL_FONT_10); c.showText("CUSTOMER REPORT"); c.endText();
            y -= 20;

            // Total (right side like A4)
            c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_9);
            String count = "Total: " + customers.size();
            tw = getThermalTextWidth(count, THERMAL_FONT_9);
            c.beginText(); c.newLineAtOffset(x + w - tw, y - THERMAL_FONT_9); c.showText(count); c.endText();
            y -= 15;

            c.setLineWidth(1f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
            y -= 20;

            // Table Header
            c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_8);
            c.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
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

            // Footer
            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            tw = PDType1Font.HELVETICA.getStringWidth("Thank you for your business!") / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - 9); c.showText("Thank you for your business!"); c.endText();
            String gen = "Generated: " + LocalDateTime.now().format(DATE_FORMAT);
            tw = PDType1Font.HELVETICA.getStringWidth(gen) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - 20); c.showText(gen); c.endText();

            c.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating thermal customer report", e);
        }
    }

    // ================================
    // 2. CUSTOMER REPORT - Professional A4
    // ================================
    public static byte[] CustomerReportA4Pro(List<CustomerEntity> customers) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream c = new PDPageContentStream(document, page);

            float m = 40f;
            float w = A4_WIDTH - 2 * m;
            float y = A4_HEIGHT - m;

            // Company Header
            c.setFont(PDType1Font.HELVETICA_BOLD, 18);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            float tw = PDType1Font.HELVETICA_BOLD.getStringWidth(COMPANY_NAME) / 1000f * 18f;
            c.beginText(); c.newLineAtOffset((A4_WIDTH - tw)/2, y - 18); c.showText(COMPANY_NAME); c.endText();
            y -= 22;

            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.3f, 0.3f, 0.3f);
            tw = PDType1Font.HELVETICA.getStringWidth(COMPANY_ADDRESS) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((A4_WIDTH - tw)/2, y - 9); c.showText(COMPANY_ADDRESS); c.endText();
            y -= 12;
            tw = PDType1Font.HELVETICA.getStringWidth(COMPANY_PHONE) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((A4_WIDTH - tw)/2, y - 9); c.showText(COMPANY_PHONE); c.endText();
            y -= 20;

            // Title (right side)
            c.setFont(PDType1Font.HELVETICA_BOLD, 26);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            tw = PDType1Font.HELVETICA_BOLD.getStringWidth("CUSTOMER REPORT") / 1000f * 26f;
            c.beginText(); c.newLineAtOffset(A4_WIDTH - m - tw, y + 25); c.showText("CUSTOMER REPORT"); c.endText();

            // Total (right side)
            c.setFont(PDType1Font.HELVETICA_BOLD, 10);
            String count = "Total: " + customers.size();
            tw = PDType1Font.HELVETICA_BOLD.getStringWidth(count) / 1000f * 10f;
            c.beginText(); c.newLineAtOffset(A4_WIDTH - m - tw, y + 8); c.showText(count); c.endText();

            y -= 10;
            c.setLineWidth(1f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
            y -= 20;

            // Table Header
            c.setFont(PDType1Font.HELVETICA_BOLD, 9);
            c.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
            c.setLineWidth(0f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            c.addRect(m, y - 16, w, 16); c.fill();
            c.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
            c.beginText(); c.newLineAtOffset(m + 10, y - 11); c.showText("ID"); c.endText();
            c.beginText(); c.newLineAtOffset(m + 100, y - 11); c.showText("Name"); c.endText();
            c.beginText(); c.newLineAtOffset(m + w - 80, y - 11); c.showText("Contact"); c.endText();
            y -= 16;

            // Table Rows
            c.setFont(PDType1Font.HELVETICA, 9);
            int idx = 1;
            for (CustomerEntity customer : customers) {
                if (y < 150) break;
                if (idx % 2 == 0) {
                    c.setNonStrokingColor(0.95f, 0.95f, 1.0f);
                    c.addRect(m, y - 14, w, 14); c.fill();
                }
                c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                String id = customer.getCustId() != null ? customer.getCustId() : "-";
                String name = truncate(customer.getName() != null ? customer.getName() : "-", 35);
                String contact = customer.getContact() != null ? customer.getContact() : "-";
                c.beginText(); c.newLineAtOffset(m + 10, y - 10); c.showText(id); c.endText();
                c.beginText(); c.newLineAtOffset(m + 100, y - 10); c.showText(name); c.endText();
                float cw = PDType1Font.HELVETICA.getStringWidth(contact) / 1000f * 9f;
                c.beginText(); c.newLineAtOffset(m + w - 5 - cw, y - 10); c.showText(contact); c.endText();
                c.setLineWidth(0.3f); c.setStrokingColor(0.8f, 0.8f, 0.8f);
                c.moveTo(m, y - 14); c.lineTo(A4_WIDTH - m, y - 14); c.stroke();
                y -= 14;
                idx++;
            }

            c.setLineWidth(1f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
            y -= 20;

            // Footer
            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            c.beginText(); c.newLineAtOffset(m, y - 9); c.showText("Thank you for your business!"); c.endText();
            String gen = "Generated: " + LocalDateTime.now().format(DATE_FORMAT);
            tw = PDType1Font.HELVETICA.getStringWidth(gen) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset(A4_WIDTH - m - tw, y - 9); c.showText(gen); c.endText();

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
            float tw = PDType1Font.HELVETICA_BOLD.getStringWidth(COMPANY_NAME) / 1000f * 18f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - 18); c.showText(COMPANY_NAME); c.endText();
            y -= 22;
            c.setLineWidth(1f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
            y -= 8;

            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.3f, 0.3f, 0.3f);
            tw = PDType1Font.HELVETICA.getStringWidth(COMPANY_ADDRESS) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - 9); c.showText(COMPANY_ADDRESS); c.endText();
            y -= 12;
            tw = PDType1Font.HELVETICA.getStringWidth(COMPANY_PHONE) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - 9); c.showText(COMPANY_PHONE); c.endText();
            y -= 15;

            // Title (centered)
            c.setFont(PDType1Font.HELVETICA_BOLD, 26);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            tw = PDType1Font.HELVETICA_BOLD.getStringWidth("PRODUCT REPORT") / 1000f * 26f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y + 25); c.showText("PRODUCT REPORT"); c.endText();

            // Total (centered)
            c.setFont(PDType1Font.HELVETICA_BOLD, 10);
            String count = "Total: " + products.size();
            tw = PDType1Font.HELVETICA_BOLD.getStringWidth(count) / 1000f * 10f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y + 8); c.showText(count); c.endText();

            y -= 10;
            c.setLineWidth(1f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
            y -= 20;

            // Table Header
            c.setFont(PDType1Font.HELVETICA_BOLD, 9);
            c.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
            c.setLineWidth(0f); c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            c.addRect(x, y - 16, w, 16); c.fill();
            c.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
            c.beginText(); c.newLineAtOffset(x + 10, y - 11); c.showText("ID"); c.endText();
            c.beginText(); c.newLineAtOffset(x + 100, y - 11); c.showText("Name"); c.endText();
            c.beginText(); c.newLineAtOffset(x + w * 0.55f, y - 11); c.showText("Price"); c.endText();
            c.beginText(); c.newLineAtOffset(x + w * 0.8f, y - 11); c.showText("Qty"); c.endText();
            y -= 16;

            // Table Rows
            c.setFont(PDType1Font.HELVETICA, 9);
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
                String id = p.getId() != null ? p.getId() : "-";
                String name = truncate(p.getName() != null ? p.getName() : "-", 12);
                String price = String.format("%.2f", p.getPrice());
                String qty = String.valueOf(p.getQty());
                c.beginText(); c.newLineAtOffset(x + 10, y - 10); c.showText(id); c.endText();
                c.beginText(); c.newLineAtOffset(x + 100, y - 10); c.showText(name); c.endText();
                float pw = PDType1Font.HELVETICA.getStringWidth(price) / 1000f * 9f;
                c.beginText(); c.newLineAtOffset(x + w * 0.55f + (w*0.25f - pw)/2, y - 10); c.showText(price); c.endText();
                float qw = PDType1Font.HELVETICA.getStringWidth(qty) / 1000f * 9f;
                c.beginText(); c.newLineAtOffset(x + w * 0.8f + (w*0.2f - qw)/2, y - 10); c.showText(qty); c.endText();
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
            tw = PDType1Font.HELVETICA.getStringWidth(lowStr) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - 9); c.showText(lowStr); c.endText();
            y -= 20;

            // Footer
            c.setLineWidth(0.3f); c.setStrokingColor(0.8f, 0.8f, 0.8f);
            c.moveTo(x, y); c.lineTo(x + w, y); c.stroke();
            y -= 10;
            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            tw = PDType1Font.HELVETICA.getStringWidth("Thank you for your business!") / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - 9); c.showText("Thank you for your business!"); c.endText();
            String gen = "Generated: " + LocalDateTime.now().format(DATE_FORMAT);
            tw = PDType1Font.HELVETICA.getStringWidth(gen) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - 20); c.showText(gen); c.endText();

            c.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating thermal product report", e);
        }
    }

    // ================================
    // 3. CUSTOMER REPORT - Professional A4
    // ================================
    // 4. PRODUCT REPORT - Professional A4
    // ================================
    public static byte[] ProductReportA4Pro(List<ProductEntity> products) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream c = new PDPageContentStream(document, page);

            float m = 40f;
            float w = A4_WIDTH - 2 * m;
            float y = A4_HEIGHT - m;

            c.setFont(PDType1Font.HELVETICA_BOLD, 18);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            c.beginText(); c.newLineAtOffset(m, y - 18); c.showText(COMPANY_NAME); c.endText();
            y -= 22;

            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.3f, 0.3f, 0.3f);
            c.beginText(); c.newLineAtOffset(m, y - 9); c.showText(COMPANY_ADDRESS); c.endText();
            y -= 12;
            c.beginText(); c.newLineAtOffset(m, y - 9); c.showText(COMPANY_PHONE); c.endText();
            y -= 20;

            c.setFont(PDType1Font.HELVETICA_BOLD, 26);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            String title = "PRODUCT REPORT";
            float tw = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000f * 26f;
            c.beginText(); c.newLineAtOffset(A4_WIDTH - m - tw, y + 25); c.showText(title); c.endText();

            String count = "Total: " + products.size();
            c.setFont(PDType1Font.HELVETICA_BOLD, 10);
            c.beginText(); c.newLineAtOffset(A4_WIDTH - m - PDType1Font.HELVETICA_BOLD.getStringWidth(count) / 1000f * 10f, y + 8); c.showText(count); c.endText();

            y -= 10;
            c.setLineWidth(1f);
            c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
            y -= 20;

            float col1 = m + 10, col2 = m + 100, col3 = m + 350, col4 = m + 430;
            c.setFont(PDType1Font.HELVETICA_BOLD, 9);
            c.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
            c.setLineWidth(0f);
            c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            c.addRect(m, y - 16, w, 16);
            c.fill();

            c.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
            c.beginText(); c.newLineAtOffset(col1, y - 11); c.showText("ID"); c.endText();
            c.beginText(); c.newLineAtOffset(col2, y - 11); c.showText("Name"); c.endText();
            c.beginText(); c.newLineAtOffset(col3, y - 11); c.showText("Price"); c.endText();
            c.beginText(); c.newLineAtOffset(col4, y - 11); c.showText("Stock"); c.endText();
            y -= 16;

            c.setFont(PDType1Font.HELVETICA, 9);
            int idx = 1;
            int lowStock = 0;
            for (ProductEntity p : products) {
                if (y < 150) break;
                if (p.getQty() < 10) lowStock++;
                if (idx % 2 == 0) {
                    c.setNonStrokingColor(0.95f, 0.95f, 1.0f);
                    c.addRect(m, y - 14, w, 14);
                    c.fill();
                }
                c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                String id = p.getId() != null ? p.getId() : "-";
                String name = truncate(p.getName() != null ? p.getName() : "-", 35);
                String price = String.format("%.2f", p.getPrice());
                String qty = String.valueOf(p.getQty());
                c.beginText(); c.newLineAtOffset(col1, y - 10); c.showText(id); c.endText();
                c.beginText(); c.newLineAtOffset(col2, y - 10); c.showText(name); c.endText();
                c.beginText(); c.newLineAtOffset(col3 + 50, y - 10); c.showText(price); c.endText();
                c.beginText(); c.newLineAtOffset(col4 + 30, y - 10); c.showText(qty); c.endText();
                c.setLineWidth(0.3f);
                c.setStrokingColor(0.8f, 0.8f, 0.8f);
                c.moveTo(m, y - 14); c.lineTo(A4_WIDTH - m, y - 14); c.stroke();
                y -= 14;
                idx++;
            }

            c.setLineWidth(1f);
            c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
            y -= 20;

            c.setFont(PDType1Font.HELVETICA, 10);
            c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            String sum = String.format("Low Stock Items: %d", lowStock);
            c.beginText(); c.newLineAtOffset(A4_WIDTH - m - PDType1Font.HELVETICA.getStringWidth(sum) / 1000f * 10f, y - 10); c.showText(sum); c.endText();

            y -= 30;
            c.setLineWidth(0.5f);
            c.setStrokingColor(0.8f, 0.8f, 0.8f);
            c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
            y -= 15;

            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            c.beginText(); c.newLineAtOffset(m, y - 9); c.showText("Thank you for your business!"); c.endText();
            String gen = "Generated: " + LocalDateTime.now().format(DATE_FORMAT);
            tw = PDType1Font.HELVETICA.getStringWidth(gen) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset(A4_WIDTH - m - tw, y - 9); c.showText(gen); c.endText();

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
    // 3. THERMAL ORDER BILL (80mm width for POS thermal printer)
    // ================================
    public static byte[] generateOrderBill(OrderEntity order, List<OrderProductDetailsEntity> details) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            float pageHeight = calculateThermalHeight(details);
            PDPage page = new PDPage(new PDRectangle(THERMAL_WIDTH, pageHeight));
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);

            float y = pageHeight - THERMAL_MARGIN;
            y = drawThermalHeader(content, y);
            y = drawThermalTitle(content, y);
            y = drawThermalDetails(content, y, order);
            y = drawThermalTable(content, y, details);
            y = drawThermalSummary(content, y, details);
            y = drawThermalPayment(content, y, details);
            drawThermalFooter(content, y);

            content.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating thermal order bill", e);
        }
    }

    private static float calculateThermalHeight(List<OrderProductDetailsEntity> details) {
        float h = 2 * THERMAL_MARGIN;
        h += 18 + 12 + 10 + 10; // Header: name + address + phone + divider
        h += 20; // Title
        h += 10 + 12 + 12 + 12 + 10; // Details + divider
        h += 12 + 4 + details.size() * 12 + 4; // Table
        h += 12 + 12 + 5 + 15 + 5; // Summary
        h += 12 + 12 + 5; // Payment
        h += 5 + 12 + 20 + 25; // Footer
        return Math.max(h, 300);
    }

    private static float drawThermalHeader(PDPageContentStream c, float y) throws IOException {
        c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_10);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        float tw = getThermalTextWidth(COMPANY_NAME, THERMAL_FONT_10);
        c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - THERMAL_FONT_10); c.showText(COMPANY_NAME); c.endText();
        y -= 18;
        c.setFont(THERMAL_FONT, THERMAL_FONT_8);
        tw = getThermalTextWidth(COMPANY_ADDRESS, THERMAL_FONT_8);
        c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - THERMAL_FONT_8); c.showText(COMPANY_ADDRESS); c.endText();
        y -= 12;
        tw = getThermalTextWidth(COMPANY_PHONE, THERMAL_FONT_8);
        c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - THERMAL_FONT_8); c.showText(COMPANY_PHONE); c.endText();
        y -= 10;
        y -= 5; drawSeparator(c, y); y -= 5;
        return y;
    }

    private static float drawThermalTitle(PDPageContentStream c, float y) throws IOException {
        String t = "INVOICE / CASH MEMO";
        c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_10);
        float tw = getThermalTextWidth(t, THERMAL_FONT_10);
        c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - THERMAL_FONT_10); c.showText(t); c.endText();
        y -= 20;
        return y;
    }

    private static float drawThermalDetails(PDPageContentStream c, float y, OrderEntity order) throws IOException {
        c.setFont(THERMAL_FONT, THERMAL_FONT_9);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        y -= 10;
        float rx = THERMAL_WIDTH - THERMAL_MARGIN;
        String cust = order.getCustomer() != null ? order.getCustomer().getName() : "-";
        String custText = "Customer: " + cust;
        float tw = getThermalTextWidth(custText, THERMAL_FONT_9);
        c.beginText(); c.newLineAtOffset(THERMAL_MARGIN, y - THERMAL_FONT_9); c.showText("Bill No: " + order.getOrderId()); c.endText();
        c.beginText(); c.newLineAtOffset(rx - tw, y - THERMAL_FONT_9); c.showText(custText); c.endText(); y -= 12;
        String dateStr = order.getDate() != null ? order.getDate().format(BILL_DATE_FORMAT) : "-";
        String contactText = "";
        if (order.getCustomer() != null && order.getCustomer().getContact() != null) {
            contactText = "Contact: " + order.getCustomer().getContact();
            tw = getThermalTextWidth(contactText, THERMAL_FONT_9);
        }
        c.beginText(); c.newLineAtOffset(THERMAL_MARGIN, y - THERMAL_FONT_9); c.showText("Date: " + dateStr); c.endText();
        if (!contactText.isEmpty()) {
            c.beginText(); c.newLineAtOffset(rx - tw, y - THERMAL_FONT_9); c.showText(contactText); c.endText();
        }
        y -= 12;
        y -= 5; drawSeparator(c, y); y -= 5;
        return y;
    }

    private static float drawThermalTable(PDPageContentStream c, float y, List<OrderProductDetailsEntity> details) throws IOException {
        c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_8);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        float colItem = THERMAL_MARGIN, colQty = THERMAL_WIDTH * 0.5f, colRate = THERMAL_WIDTH * 0.7f, colAmt = THERMAL_WIDTH * 0.85f;
        c.beginText(); c.newLineAtOffset(colItem, y - THERMAL_FONT_8); c.showText("Item"); c.endText();
        c.beginText(); c.newLineAtOffset(colQty, y - THERMAL_FONT_8); c.showText("Qty"); c.endText();
        c.beginText(); c.newLineAtOffset(colRate, y - THERMAL_FONT_8); c.showText("Rate"); c.endText();
        c.beginText(); c.newLineAtOffset(colAmt, y - THERMAL_FONT_8); c.showText("Amount"); c.endText();
        y -= 12;
        drawSeparator(c, y); y -= 4;
        c.setFont(THERMAL_FONT, THERMAL_FONT_8);
        for (OrderProductDetailsEntity d : details) {
            if (y < THERMAL_MARGIN + 50) break;
            String nm = d.getProduct() != null ? truncate(d.getProduct().getName(), 15) : "-";
            String qty = String.valueOf(d.getQuantity());
            String rate = String.format("%.2f", d.getUnitPrice());
            String amt = String.format("%.2f", d.getQuantity() * d.getUnitPrice());
            c.beginText(); c.newLineAtOffset(colItem, y - THERMAL_FONT_8); c.showText(nm); c.endText();
            float qw = getThermalTextWidth(qty, THERMAL_FONT_8);
            c.beginText(); c.newLineAtOffset(colQty + (THERMAL_WIDTH*0.2f - qw)/2, y - THERMAL_FONT_8); c.showText(qty); c.endText();
            float rw = getThermalTextWidth(rate, THERMAL_FONT_8);
            c.beginText(); c.newLineAtOffset(colRate + (THERMAL_WIDTH*0.15f - rw)/2, y - THERMAL_FONT_8); c.showText(rate); c.endText();
            float aw = getThermalTextWidth(amt, THERMAL_FONT_8);
            c.beginText(); c.newLineAtOffset(colAmt + (THERMAL_WIDTH*0.15f - aw)/2, y - THERMAL_FONT_8); c.showText(amt); c.endText();
            y -= 12;
        }
        y -= 4; drawSeparator(c, y); y -= 4;
        return y;
    }

    private static float drawThermalSummary(PDPageContentStream c, float y, List<OrderProductDetailsEntity> details) throws IOException {
        c.setFont(THERMAL_FONT, THERMAL_FONT_9);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        double sub = 0; for (OrderProductDetailsEntity d : details) sub += d.getQuantity() * d.getUnitPrice();
        double tax = sub * 0.05, net = sub + tax;
        float rx = THERMAL_WIDTH - THERMAL_MARGIN;
        String s1 = String.format("Sub Total: %.2f", sub);
        float tw = getThermalTextWidth(s1, THERMAL_FONT_9);
        c.beginText(); c.newLineAtOffset(rx - tw, y - THERMAL_FONT_9); c.showText(s1); c.endText(); y -= 12;
        String s2 = String.format("Tax (5%%): %.2f", tax);
        tw = getThermalTextWidth(s2, THERMAL_FONT_9);
        c.beginText(); c.newLineAtOffset(rx - tw, y - THERMAL_FONT_9); c.showText(s2); c.endText(); y -= 12;
        y -= 5; drawSeparator(c, y); y -= 5;
        c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_10);
        String s3 = String.format("NET TOTAL: %.2f", net);
        tw = getThermalTextWidth(s3, THERMAL_FONT_10);
        c.beginText(); c.newLineAtOffset(rx - tw, y - THERMAL_FONT_10); c.showText(s3); c.endText(); y -= 15;
        y -= 5; drawSeparator(c, y); y -= 5;
        return y;
    }

    private static float drawThermalPayment(PDPageContentStream c, float y, List<OrderProductDetailsEntity> details) throws IOException {
        c.setFont(THERMAL_FONT, THERMAL_FONT_9);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        double sub = 0; for (OrderProductDetailsEntity d : details) sub += d.getQuantity() * d.getUnitPrice();
        double tax = sub * 0.05, net = sub + tax;
        float rx = THERMAL_WIDTH - THERMAL_MARGIN;
        String s1 = String.format("Paid: %.2f", net);
        float tw = getThermalTextWidth(s1, THERMAL_FONT_9);
        c.beginText(); c.newLineAtOffset(rx - tw, y - THERMAL_FONT_9); c.showText(s1); c.endText(); y -= 12;
        String s2 = "Balance: 0.00";
        tw = getThermalTextWidth(s2, THERMAL_FONT_9);
        c.beginText(); c.newLineAtOffset(rx - tw, y - THERMAL_FONT_9); c.showText(s2); c.endText(); y -= 12;
        y -= 5; drawSeparator(c, y); y -= 5;
        return y;
    }

    private static float drawThermalFooter(PDPageContentStream c, float y) throws IOException {
        y -= 5; drawSeparator(c, y); y -= 5;
        c.setFont(THERMAL_FONT, THERMAL_FONT_8);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        String dt = LocalDateTime.now().format(DATE_FORMAT);
        float tw = getThermalTextWidth(dt, THERMAL_FONT_8);
        c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - THERMAL_FONT_8); c.showText(dt); c.endText();
        y -= 12;
        c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_9);
        String ty = "Thank You! Visit Again";
        tw = getThermalTextWidth(ty, THERMAL_FONT_9);
        c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - THERMAL_FONT_9); c.showText(ty); c.endText();
        y -= 20;
        c.setLineWidth(0.5f);
        float sw = 60f, ss = (THERMAL_WIDTH - sw)/2;
        c.moveTo(ss, y); c.lineTo(ss + sw, y); c.stroke();
        y -= 12;
        c.setFont(THERMAL_FONT, THERMAL_FONT_8);
        String sig = "Signature";
        tw = getThermalTextWidth(sig, THERMAL_FONT_8);
        c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - THERMAL_FONT_8); c.showText(sig); c.endText();
        return y;
    }

    private static void drawSeparator(PDPageContentStream c, float y) throws IOException {
        c.setFont(THERMAL_FONT, THERMAL_FONT_8);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        float tw = getThermalTextWidth(SEPARATOR, THERMAL_FONT_8);
        c.beginText(); c.newLineAtOffset((THERMAL_WIDTH - tw)/2, y - THERMAL_FONT_8); c.showText(SEPARATOR); c.endText();
    }

    private static float getThermalTextWidth(String text, float fontSize) throws IOException {
        if (text == null) return 0;
        return (THERMAL_FONT.getStringWidth(text) / 1000f) * fontSize;
    }

    // ================================
    // 4. A4 THERMAL-STYLE ORDER BILL (same layout as 80mm, centered on A4)
    // ================================
    public static byte[] generateOrderBillA4(OrderEntity order, List<OrderProductDetailsEntity> details) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            float pageHeight = calculateThermalHeight(details) + 50;
            PDPage page = new PDPage(new PDRectangle(A4_WIDTH, pageHeight));
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);

            float xOffset = A4_BILL_MARGIN;
            float y = pageHeight - 25 * MM_TO_POINTS;

            y = drawA4ThermalHeader(content, xOffset, y);
            y = drawA4ThermalTitle(content, xOffset, y);
            y = drawA4ThermalDetails(content, xOffset, y, order);
            y = drawA4ThermalTable(content, xOffset, y, details);
            y = drawA4ThermalSummary(content, xOffset, y, details);
            y = drawA4ThermalPayment(content, xOffset, y, details);
            drawA4ThermalFooter(content, xOffset, y);

            content.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating A4 thermal-style order bill", e);
        }
    }

    private static float drawA4ThermalHeader(PDPageContentStream c, float x, float y) throws IOException {
        c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_10);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        float tw = getThermalTextWidth(COMPANY_NAME, THERMAL_FONT_10);
        c.beginText(); c.newLineAtOffset(x + (A4_BILL_WIDTH - tw)/2, y - THERMAL_FONT_10); c.showText(COMPANY_NAME); c.endText();
        y -= 18;
        c.setFont(THERMAL_FONT, THERMAL_FONT_8);
        tw = getThermalTextWidth(COMPANY_ADDRESS, THERMAL_FONT_8);
        c.beginText(); c.newLineAtOffset(x + (A4_BILL_WIDTH - tw)/2, y - THERMAL_FONT_8); c.showText(COMPANY_ADDRESS); c.endText();
        y -= 12;
        tw = getThermalTextWidth(COMPANY_PHONE, THERMAL_FONT_8);
        c.beginText(); c.newLineAtOffset(x + (A4_BILL_WIDTH - tw)/2, y - THERMAL_FONT_8); c.showText(COMPANY_PHONE); c.endText();
        y -= 10;
        y -= 5; drawA4Separator(c, x, y); y -= 5;
        return y;
    }

    private static float drawA4ThermalTitle(PDPageContentStream c, float x, float y) throws IOException {
        String t = "INVOICE / CASH MEMO";
        c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_10);
        float tw = getThermalTextWidth(t, THERMAL_FONT_10);
        c.beginText(); c.newLineAtOffset(x + (A4_BILL_WIDTH - tw)/2, y - THERMAL_FONT_10); c.showText(t); c.endText();
        y -= 20;
        return y;
    }

    private static float drawA4ThermalDetails(PDPageContentStream c, float x, float y, OrderEntity order) throws IOException {
        c.setFont(THERMAL_FONT, THERMAL_FONT_9);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        y -= 10;
        c.beginText(); c.newLineAtOffset(x + THERMAL_MARGIN, y - THERMAL_FONT_9); c.showText("Bill No: " + order.getOrderId()); c.endText();
        String cust = order.getCustomer() != null ? order.getCustomer().getName() : "-";
        String custText = "Customer: " + cust;
        float tw = getThermalTextWidth(custText, THERMAL_FONT_9);
        c.beginText(); c.newLineAtOffset(x + A4_BILL_WIDTH - THERMAL_MARGIN - tw, y - THERMAL_FONT_9); c.showText(custText); c.endText(); y -= 12;
        String dateStr = order.getDate() != null ? order.getDate().format(BILL_DATE_FORMAT) : "-";
        c.beginText(); c.newLineAtOffset(x + THERMAL_MARGIN, y - THERMAL_FONT_9); c.showText("Date: " + dateStr); c.endText();
        if (order.getCustomer() != null && order.getCustomer().getContact() != null) {
            String contactText = "Contact: " + order.getCustomer().getContact();
            tw = getThermalTextWidth(contactText, THERMAL_FONT_9);
            c.beginText(); c.newLineAtOffset(x + A4_BILL_WIDTH - THERMAL_MARGIN - tw, y - THERMAL_FONT_9); c.showText(contactText); c.endText();
        }
        y -= 12;
        y -= 5; drawA4Separator(c, x, y); y -= 5;
        return y;
    }

    private static float drawA4ThermalTable(PDPageContentStream c, float x, float y, List<OrderProductDetailsEntity> details) throws IOException {
        c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_8);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        float colItem = x + THERMAL_MARGIN, colQty = x + A4_BILL_WIDTH * 0.5f, colRate = x + A4_BILL_WIDTH * 0.7f, colAmt = x + A4_BILL_WIDTH * 0.85f;
        c.beginText(); c.newLineAtOffset(colItem, y - THERMAL_FONT_8); c.showText("Item"); c.endText();
        c.beginText(); c.newLineAtOffset(colQty, y - THERMAL_FONT_8); c.showText("Qty"); c.endText();
        c.beginText(); c.newLineAtOffset(colRate, y - THERMAL_FONT_8); c.showText("Rate"); c.endText();
        c.beginText(); c.newLineAtOffset(colAmt, y - THERMAL_FONT_8); c.showText("Amount"); c.endText();
        y -= 12;
        drawA4Separator(c, x, y); y -= 4;
        c.setFont(THERMAL_FONT, THERMAL_FONT_8);
        for (OrderProductDetailsEntity d : details) {
            if (y < 100) break;
            String nm = d.getProduct() != null ? truncate(d.getProduct().getName(), 15) : "-";
            String qty = String.valueOf(d.getQuantity());
            String rate = String.format("%.2f", d.getUnitPrice());
            String amt = String.format("%.2f", d.getQuantity() * d.getUnitPrice());
            c.beginText(); c.newLineAtOffset(colItem, y - THERMAL_FONT_8); c.showText(nm); c.endText();
            float qw = getThermalTextWidth(qty, THERMAL_FONT_8);
            c.beginText(); c.newLineAtOffset(colQty + (A4_BILL_WIDTH*0.2f - qw)/2, y - THERMAL_FONT_8); c.showText(qty); c.endText();
            float rw = getThermalTextWidth(rate, THERMAL_FONT_8);
            c.beginText(); c.newLineAtOffset(colRate + (A4_BILL_WIDTH*0.15f - rw)/2, y - THERMAL_FONT_8); c.showText(rate); c.endText();
            float aw = getThermalTextWidth(amt, THERMAL_FONT_8);
            c.beginText(); c.newLineAtOffset(colAmt + (A4_BILL_WIDTH*0.15f - aw)/2, y - THERMAL_FONT_8); c.showText(amt); c.endText();
            y -= 12;
        }
        y -= 4; drawA4Separator(c, x, y); y -= 4;
        return y;
    }

    private static float drawA4ThermalSummary(PDPageContentStream c, float x, float y, List<OrderProductDetailsEntity> details) throws IOException {
        c.setFont(THERMAL_FONT, THERMAL_FONT_9);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        double sub = 0; for (OrderProductDetailsEntity d : details) sub += d.getQuantity() * d.getUnitPrice();
        double tax = sub * 0.05, net = sub + tax;
        float rx = x + A4_BILL_WIDTH - THERMAL_MARGIN;
        String s1 = String.format("Sub Total: %.2f", sub);
        float tw = getThermalTextWidth(s1, THERMAL_FONT_9);
        c.beginText(); c.newLineAtOffset(rx - tw, y - THERMAL_FONT_9); c.showText(s1); c.endText(); y -= 12;
        String s2 = String.format("Tax (5%%): %.2f", tax);
        tw = getThermalTextWidth(s2, THERMAL_FONT_9);
        c.beginText(); c.newLineAtOffset(rx - tw, y - THERMAL_FONT_9); c.showText(s2); c.endText(); y -= 12;
        y -= 5; drawA4Separator(c, x, y); y -= 5;
        c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_10);
        String s3 = String.format("NET TOTAL: %.2f", net);
        tw = getThermalTextWidth(s3, THERMAL_FONT_10);
        c.beginText(); c.newLineAtOffset(rx - tw, y - THERMAL_FONT_10); c.showText(s3); c.endText(); y -= 15;
        y -= 5; drawA4Separator(c, x, y); y -= 5;
        return y;
    }

    private static float drawA4ThermalPayment(PDPageContentStream c, float x, float y, List<OrderProductDetailsEntity> details) throws IOException {
        c.setFont(THERMAL_FONT, THERMAL_FONT_9);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        double sub = 0; for (OrderProductDetailsEntity d : details) sub += d.getQuantity() * d.getUnitPrice();
        double tax = sub * 0.05, net = sub + tax;
        float rx = x + A4_BILL_WIDTH - THERMAL_MARGIN;
        String s1 = String.format("Paid: %.2f", net);
        float tw = getThermalTextWidth(s1, THERMAL_FONT_9);
        c.beginText(); c.newLineAtOffset(rx - tw, y - THERMAL_FONT_9); c.showText(s1); c.endText(); y -= 12;
        String s2 = "Balance: 0.00";
        tw = getThermalTextWidth(s2, THERMAL_FONT_9);
        c.beginText(); c.newLineAtOffset(rx - tw, y - THERMAL_FONT_9); c.showText(s2); c.endText(); y -= 12;
        y -= 5; drawA4Separator(c, x, y); y -= 5;
        return y;
    }

    private static float drawA4ThermalFooter(PDPageContentStream c, float x, float y) throws IOException {
        y -= 5; drawA4Separator(c, x, y); y -= 5;
        c.setFont(THERMAL_FONT, THERMAL_FONT_8);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        String dt = LocalDateTime.now().format(DATE_FORMAT);
        float tw = getThermalTextWidth(dt, THERMAL_FONT_8);
        c.beginText(); c.newLineAtOffset(x + (A4_BILL_WIDTH - tw)/2, y - THERMAL_FONT_8); c.showText(dt); c.endText();
        y -= 12;
        c.setFont(THERMAL_FONT_BOLD, THERMAL_FONT_9);
        String ty = "Thank You! Visit Again";
        tw = getThermalTextWidth(ty, THERMAL_FONT_9);
        c.beginText(); c.newLineAtOffset(x + (A4_BILL_WIDTH - tw)/2, y - THERMAL_FONT_9); c.showText(ty); c.endText();
        y -= 20;
        c.setLineWidth(0.5f);
        float sw = 60f, ss = x + (A4_BILL_WIDTH - sw)/2;
        c.moveTo(ss, y); c.lineTo(ss + sw, y); c.stroke();
        y -= 12;
        c.setFont(THERMAL_FONT, THERMAL_FONT_8);
        String sig = "Signature";
        tw = getThermalTextWidth(sig, THERMAL_FONT_8);
        c.beginText(); c.newLineAtOffset(x + (A4_BILL_WIDTH - tw)/2, y - THERMAL_FONT_8); c.showText(sig); c.endText();
        return y;
    }

    private static void drawA4Separator(PDPageContentStream c, float x, float y) throws IOException {
        c.setFont(THERMAL_FONT, THERMAL_FONT_8);
        c.setNonStrokingColor(COLOR_BLACK[0], COLOR_BLACK[1], COLOR_BLACK[2]);
        float tw = getThermalTextWidth(SEPARATOR, THERMAL_FONT_8);
        c.beginText(); c.newLineAtOffset(x + (A4_BILL_WIDTH - tw)/2, y - THERMAL_FONT_8); c.showText(SEPARATOR); c.endText();
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

            float m = 40f; // margin
            float w = A4_WIDTH - 2 * m;
            float y = A4_HEIGHT - m;

            // Header
            c.setFont(PDType1Font.HELVETICA_BOLD, 18);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            c.beginText(); c.newLineAtOffset(m, y - 18); c.showText(COMPANY_NAME); c.endText();
            y -= 22;

            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.3f, 0.3f, 0.3f);
            c.beginText(); c.newLineAtOffset(m, y - 9); c.showText(COMPANY_ADDRESS); c.endText(); y -= 12;
            c.beginText(); c.newLineAtOffset(m, y - 9); c.showText(COMPANY_PHONE); c.endText();
            y -= 20;

            // INVOICE title (right)
            c.setFont(PDType1Font.HELVETICA_BOLD, 26);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            String title = "INVOICE";
            float tw = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000f * 26f;
            c.beginText(); c.newLineAtOffset(A4_WIDTH - m - tw, y + 25); c.showText(title); c.endText();

            // Invoice details (right)
            c.setFont(PDType1Font.HELVETICA_BOLD, 10);
            c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            String invNo = "Invoice #: " + order.getOrderId();
            tw = PDType1Font.HELVETICA_BOLD.getStringWidth(invNo) / 1000f * 10f;
            c.beginText(); c.newLineAtOffset(A4_WIDTH - m - tw, y + 8); c.showText(invNo); c.endText();
            String dateStr = order.getDate() != null ? order.getDate().format(BILL_DATE_FORMAT) : "-";
            String invDate = "Date: " + dateStr;
            c.beginText(); c.newLineAtOffset(A4_WIDTH - m - PDType1Font.HELVETICA_BOLD.getStringWidth(invDate) / 1000f * 10f, y - 4); c.showText(invDate); c.endText();

            y -= 10;

            // Divider
            c.setLineWidth(1f);
            c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
            y -= 20;

            // Bill To
            c.setFont(PDType1Font.HELVETICA_BOLD, 11);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            c.beginText(); c.newLineAtOffset(m, y - 11); c.showText("BILL TO:"); c.endText();
            y -= 18;

            c.setFont(PDType1Font.HELVETICA, 10);
            c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            String custName = order.getCustomer() != null ? order.getCustomer().getName() : "-";
            c.beginText(); c.newLineAtOffset(m, y - 10); c.showText(custName); c.endText(); y -= 14;
            if (order.getCustomer() != null && order.getCustomer().getContact() != null) {
                c.beginText(); c.newLineAtOffset(m, y - 10); c.showText("Contact: " + order.getCustomer().getContact()); c.endText(); y -= 14;
            }

            y -= 10;

            // Table Header
            float tableY = y;
            c.setFont(PDType1Font.HELVETICA_BOLD, 9);
            c.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
            c.setLineWidth(0f);
            c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            c.addRect(m, tableY - 16, w, 16);
            c.fill();

            c.setNonStrokingColor(COLOR_WHITE[0], COLOR_WHITE[1], COLOR_WHITE[2]);
            float col1 = m + 10, col2 = m + 50, col3 = m + 280, col4 = m + 330, col5 = m + 380, col6 = m + 440;
            c.beginText(); c.newLineAtOffset(col1, tableY - 11); c.showText("#"); c.endText();
            c.beginText(); c.newLineAtOffset(col2, tableY - 11); c.showText("Description"); c.endText();
            c.beginText(); c.newLineAtOffset(col3, tableY - 11); c.showText("Qty"); c.endText();
            c.beginText(); c.newLineAtOffset(col4, tableY - 11); c.showText("Rate"); c.endText();
            c.beginText(); c.newLineAtOffset(col5, tableY - 11); c.showText("Tax"); c.endText();
            c.beginText(); c.newLineAtOffset(col6, tableY - 11); c.showText("Amount"); c.endText();

            y = tableY - 16;

            // Table Rows
            c.setFont(PDType1Font.HELVETICA, 9);
            int idx = 1;
            double grandTotal = 0;
            for (OrderProductDetailsEntity d : details) {
                if (y < 150) break;
                double lineTotal = d.getQuantity() * d.getUnitPrice();
                grandTotal += lineTotal;

                // Alternating background
                if (idx % 2 == 0) {
                    c.setNonStrokingColor(0.95f, 0.95f, 1.0f);
                    c.addRect(m, y - 14, w, 14);
                    c.fill();
                }

                c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                String nm = d.getProduct() != null ? d.getProduct().getName() : "-";
                c.beginText(); c.newLineAtOffset(col1, y - 10); c.showText(String.valueOf(idx)); c.endText();
                c.beginText(); c.newLineAtOffset(col2, y - 10); c.showText(truncate(nm, 30)); c.endText();
                c.beginText(); c.newLineAtOffset(col3 + 15, y - 10); c.showText(String.valueOf(d.getQuantity())); c.endText();
                String rate = String.format("%.2f", d.getUnitPrice());
                c.beginText(); c.newLineAtOffset(col4 + 30, y - 10); c.showText(rate); c.endText();
                c.beginText(); c.newLineAtOffset(col5 + 20, y - 10); c.showText("5%"); c.endText();
                String amt = String.format("%.2f", lineTotal);
                c.beginText(); c.newLineAtOffset(col6 + 40, y - 10); c.showText(amt); c.endText();

                // Row border
                c.setLineWidth(0.3f);
                c.setStrokingColor(0.8f, 0.8f, 0.8f);
                c.moveTo(m, y - 14); c.lineTo(A4_WIDTH - m, y - 14); c.stroke();

                y -= 14;
                idx++;
            }

            // Bottom border
            c.setLineWidth(1f);
            c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
            y -= 20;

            // Summary (right)
            float sumX = A4_WIDTH - m - 200;
            c.setFont(PDType1Font.HELVETICA, 10);
            c.setNonStrokingColor(0.1f, 0.1f, 0.1f);
            String subStr = String.format("Sub Total: %,.2f", grandTotal);
            tw = PDType1Font.HELVETICA.getStringWidth(subStr) / 1000f * 10f;
            c.beginText(); c.newLineAtOffset(sumX + 200 - tw, y - 10); c.showText(subStr); c.endText(); y -= 16;
            double tax = grandTotal * 0.05;
            String taxStr = String.format("Tax (5%%): %,.2f", tax);
            tw = PDType1Font.HELVETICA.getStringWidth(taxStr) / 1000f * 10f;
            c.beginText(); c.newLineAtOffset(sumX + 200 - tw, y - 10); c.showText(taxStr); c.endText(); y -= 16;

            c.setLineWidth(0.5f);
            c.setStrokingColor(0.1f, 0.1f, 0.3f);
            c.moveTo(sumX, y + 6); c.lineTo(sumX + 200, y + 6); c.stroke();
            y -= 10;

            c.setFont(PDType1Font.HELVETICA_BOLD, 12);
            c.setNonStrokingColor(0.1f, 0.1f, 0.3f);
            String totalStr = String.format("TOTAL: %,.2f", grandTotal + tax);
            tw = PDType1Font.HELVETICA_BOLD.getStringWidth(totalStr) / 1000f * 12f;
            c.beginText(); c.newLineAtOffset(sumX + 200 - tw, y - 12); c.showText(totalStr); c.endText();
            y -= 30;

            // Footer
            c.setLineWidth(0.5f);
            c.setStrokingColor(0.8f, 0.8f, 0.8f);
            c.moveTo(m, y); c.lineTo(A4_WIDTH - m, y); c.stroke();
            y -= 15;

            c.setFont(PDType1Font.HELVETICA, 9);
            c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            c.beginText(); c.newLineAtOffset(m, y - 9); c.showText("Thank you for your business!"); c.endText();
            String gen = "Generated: " + LocalDateTime.now().format(DATE_FORMAT);
            tw = PDType1Font.HELVETICA.getStringWidth(gen) / 1000f * 9f;
            c.beginText(); c.newLineAtOffset(A4_WIDTH - m - tw, y - 9); c.showText(gen); c.endText();

            c.close();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating A4 invoice", e);
        }
    }
}