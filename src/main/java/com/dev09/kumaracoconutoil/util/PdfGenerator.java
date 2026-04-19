package com.dev09.kumaracoconutoil.util;

import com.dev09.kumaracoconutoil.entity.CustomerEntity;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfGenerator {

    private static final float MM_TO_POINTS = 2.83465f;
    private static final float PAPER_WIDTH_MM = 80f;
    private static final float PAPER_WIDTH = PAPER_WIDTH_MM * MM_TO_POINTS;

    private static final float LEFT_MARGIN = 12;
    private static final float RIGHT_MARGIN = 12;
    private static final float LINE_HEIGHT = 15;

    public static byte[] exportAllCustomers(List<CustomerEntity> customers) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(new PDRectangle(PAPER_WIDTH, 720));
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            float y = page.getMediaBox().getHeight() - 35;

            // ====================== HEADER ======================
            centerText(content, "DILSHAN OIL MILL", y, 13);
            y -= 16;
            centerText(content, "309/a 5 Baddegoda Rd, Bandaragama 12530", y, 8);
            y -= 11;
            centerText(content, "Tel: 077-6518819 | 012-3456789", y, 8);
            y -= 22;

            drawLine(content, y);
            y -= 18;

            // ====================== TITLE ======================
            centerText(content, "ALL CUSTOMER REPORT", y, 10);
            y -= 22;

            // ====================== TABLE HEADER ======================
            content.setFont(PDType1Font.HELVETICA_BOLD, 8);

            // ID
            content.beginText();
            content.newLineAtOffset(LEFT_MARGIN, y);
            content.showText("ID");
            content.endText();

            // Name
            content.beginText();
            content.newLineAtOffset(LEFT_MARGIN + 38, y);
            content.showText("Name");
            content.endText();

            // Contact (Right Aligned)
            float contactX = PAPER_WIDTH - RIGHT_MARGIN - getTextWidth("Contact", 8);
            content.beginText();
            content.newLineAtOffset(contactX, y);
            content.showText("Contact");
            content.endText();

            y -= 12;
            drawLine(content, y);
            y -= 14;

            // ====================== DATA ROWS ======================
            content.setFont(PDType1Font.HELVETICA, 8);

            for (CustomerEntity c : customers) {
                if (y < 80) break;

                String id = safe(c.getCustId());
                String name = formatField(safe(c.getName()), 25);
                String contact = formatField(safe(c.getContact()), 0);

                // ID
                content.beginText();
                content.newLineAtOffset(LEFT_MARGIN, y);
                content.showText(id);
                content.endText();

                // Name
                content.beginText();
                content.newLineAtOffset(LEFT_MARGIN + 38, y);
                content.showText(name);
                content.endText();

                // Contact - Right Aligned
                float contactPos = PAPER_WIDTH - RIGHT_MARGIN - getTextWidth(contact, 8);
                content.beginText();
                content.newLineAtOffset(contactPos, y);
                content.showText(contact);
                content.endText();

                y -= LINE_HEIGHT;
            }

            y -= 12;
            drawLine(content, y);
            y -= 18;

            // ====================== FOOTER ======================
            centerText(content, "Total Customers : " + customers.size(), y, 9);
            y -= 14;

            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            centerText(content, "Generated : " + date, y, 7);
            y -= 18;

            centerText(content, "Thank You!", y, 11);

            content.close();
            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ====================== HELPER METHODS ======================
    private static void centerText(PDPageContentStream content, String text, float y, float fontSize) throws IOException {
        content.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
        float textWidth = getTextWidth(text, fontSize);
        float startX = (PAPER_WIDTH - textWidth) / 2;

        content.beginText();
        content.newLineAtOffset(startX, y);
        content.showText(text);
        content.endText();
    }

    private static float getTextWidth(String text, float fontSize) throws IOException {
        return (PDType1Font.HELVETICA.getStringWidth(text) / 1000f) * fontSize;
    }

    private static void drawLine(PDPageContentStream content, float y) throws IOException {
        content.moveTo(LEFT_MARGIN - 2, y);
        content.lineTo(PAPER_WIDTH - LEFT_MARGIN + 2, y);
        content.stroke();
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max - 1) + "…" : text;
    }

    private static String safe(String str) {
        return str == null ? "" : str.trim();
    }

    /** New method: Returns "-" if field is empty */
    private static String formatField(String value, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        if (maxLength > 0) {
            return truncate(value, maxLength);
        }
        return value;
    }
}