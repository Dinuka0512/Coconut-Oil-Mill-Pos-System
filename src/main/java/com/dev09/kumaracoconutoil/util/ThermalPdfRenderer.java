package com.dev09.kumaracoconutoil.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Converts a generated ESC/POS receipt into a small readable PDF and opens it.
 *
 * <p>Used as a fallback when no thermal printer is available: the cashier can still
 * see exactly the receipt text that would have gone to the 80mm printer.</p>
 */
final class ThermalPdfRenderer {

    private static final float PAGE_WIDTH = 226.77f; // 80mm in points
    private static final float PAGE_HEIGHT = 841.89f; // 297mm in points
    private static final float MARGIN = 12f;
    private static final float FONT_SIZE = 11f;
    private static final float LEADING = 14f;

    private ThermalPdfRenderer() {
    }

    static void open(byte[] escpos) {
        try {
            byte[] pdf = toPdf(escpos);
            Path temp = Files.createTempFile("dilshan-80mm-", ".pdf");
            Files.write(temp, pdf);
            if (!Desktop.isDesktopSupported()) {
                throw new IllegalStateException("Desktop is not supported on this machine.");
            }
            Desktop.getDesktop().open(temp.toFile());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to open 80mm PDF preview: " + e.getMessage(), e);
        }
    }

    static byte[] toPdf(byte[] escpos) {
        String[] lines = decodeEscPos(escpos).split("\n", -1);
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);
            content.setFont(PDType1Font.COURIER, FONT_SIZE);
            content.setNonStrokingColor(java.awt.Color.BLACK);

            float y = PAGE_HEIGHT - MARGIN - FONT_SIZE;
            for (String line : lines) {
                if (y < MARGIN + FONT_SIZE) {
                    content.close();
                    page = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    content.setFont(PDType1Font.COURIER, FONT_SIZE);
                    content.setNonStrokingColor(java.awt.Color.BLACK);
                    y = PAGE_HEIGHT - MARGIN - FONT_SIZE;
                }
                content.beginText();
                content.newLineAtOffset(MARGIN, y);
                content.showText(fit(line));
                content.endText();
                y -= LEADING;
            }
            content.close();
            document.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to render 80mm PDF preview", e);
        }
    }

    private static String fit(String value) {
        String safe = value == null ? "" : value.replace('\r', ' ');
        // Courier is monospaced: 24 columns at 11pt fits comfortably in 80mm.
        return safe.length() <= 24 ? safe : safe.substring(0, 24);
    }

    /**
     * Strips ESC/POS control sequences and keeps only printable characters and
     * line feeds. escpos-coffee uses standard ESC/GS command families, so a
     * small state machine is enough for a faithful plain-text preview.
     */
    private static String decodeEscPos(byte[] bytes) {
        StringBuilder text = new StringBuilder();
        int i = 0;
        while (i < bytes.length) {
            int b = bytes[i] & 0xFF;

            if (b == 0x1B || b == 0x1D) { // ESC or GS
                if (i + 1 >= bytes.length) {
                    break;
                }
                int command = bytes[i + 1] & 0xFF;
                int skip = 2;
                if (command == '(') {
                    if (i + 3 < bytes.length) {
                        int length = (bytes[i + 2] & 0xFF) | ((bytes[i + 3] & 0xFF) << 8);
                        skip = 4 + length;
                    }
                } else if (command == '!' || command == 'a' || command == 'd'
                        || command == 'V' || command == 't' || command == 'r'
                        || command == 'U' || command == 'B' || command == 'T'
                        || command == 'M' || command == 'E' || command == '-'
                        || command == ' ' || command == 'c' || command == 'R') {
                    skip = 3;
                }
                i += Math.min(skip, bytes.length - i);
                continue;
            }

            if (b == '\n') {
                text.append('\n');
            } else if (b == '\r') {
                // ignore; line feeds already preserve the layout
            } else if (b >= 0x20 && b <= 0x7E) {
                text.append((char) b);
            } else if (b >= 0xA0) {
                text.append(new String(new byte[]{(byte) b}, Charset.forName("Cp1252")));
            }
            i++;
        }
        return text.toString();
    }
}
