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
    private static final float MARGIN = 20f * MM_TO_POINTS;

    private static final String COMPANY_NAME = "DILSHAN OIL MILL";
    private static final String COMPANY_TAGLINE = "Your Trusted Partner in Coconut Products";
    private static final String COMPANY_ADDRESS = "309/a 5 Baddegoda Rd, Bandaragama 12530";
    private static final String COMPANY_PHONE = "Tel: 077-6518819 | 012-3456789";
    private static final String DEVELOPED_BY = "Developed by Dev09Solutions.com";

    private static final float COLOR_BLACK = 0.0f;
    private static final float COLOR_WHITE = 1.0f;
    private static final float COLOR_GRAY = 0.85f;
    private static final float COLOR_DARK = 0.20f;

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
            float contentWidth = A4_WIDTH - (2 * margin);
            float y = A4_HEIGHT - margin;

            // ================================
            // HEADER SECTION (Centered)
            // ================================
            content.setFont(PDType1Font.HELVETICA_BOLD, 18);
            content.setNonStrokingColor(COLOR_DARK, COLOR_DARK, COLOR_DARK);
            float titleWidth = getTextWidth(COMPANY_NAME, 18);
            content.beginText();
            content.newLineAtOffset((A4_WIDTH - titleWidth) / 2, y);
            content.showText(COMPANY_NAME);
            content.endText();

            y -= 18;
            content.setFont(PDType1Font.HELVETICA, 10);
            content.setNonStrokingColor(COLOR_GRAY, COLOR_GRAY, COLOR_GRAY);
            float taglineWidth = getTextWidth(COMPANY_TAGLINE, 10);
            content.beginText();
            content.newLineAtOffset((A4_WIDTH - taglineWidth) / 2, y);
            content.showText(COMPANY_TAGLINE);
            content.endText();

            y -= 16;
            content.setFont(PDType1Font.HELVETICA, 9);
            content.setNonStrokingColor(COLOR_DARK, COLOR_DARK, COLOR_DARK);
            float addrWidth = getTextWidth(COMPANY_ADDRESS, 9);
            content.beginText();
            content.newLineAtOffset((A4_WIDTH - addrWidth) / 2, y);
            content.showText(COMPANY_ADDRESS);
            content.endText();

            y -= 14;
            float phoneWidth = getTextWidth(COMPANY_PHONE, 9);
            content.beginText();
            content.newLineAtOffset((A4_WIDTH - phoneWidth) / 2, y);
            content.showText(COMPANY_PHONE);
            content.endText();

            // Divider line
            y -= 18;
            content.setLineWidth(1f);
            content.setStrokingColor(COLOR_DARK, COLOR_DARK, COLOR_DARK);
            content.moveTo(margin, y);
            content.lineTo(A4_WIDTH - margin, y);
            content.stroke();

            // ================================
            // REPORT TITLE SECTION (Centered)
            // ================================
            y -= 30;
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            float reportTitleWidth = getTextWidth("CUSTOMER REPORT", 14);
            content.beginText();
            content.newLineAtOffset((A4_WIDTH - reportTitleWidth) / 2, y);
            content.showText("CUSTOMER REPORT");
            content.endText();

            // Thin separator line
            y -= 12;
            content.setLineWidth(0.5f);
            content.moveTo(margin + 50, y);
            content.lineTo(A4_WIDTH - margin - 50, y);
            content.stroke();

            // ================================
            // TABLE SECTION
            // ================================
            y -= 25;
            float colId = margin + 10;
            float colName = margin + 80;
            float colContact = margin + 230;

            // Table header (Black background, White text)
            content.setNonStrokingColor(COLOR_BLACK, COLOR_BLACK, COLOR_BLACK);
            content.addRect(margin, y - 16, contentWidth, 16);
            content.fill();

            content.setFont(PDType1Font.HELVETICA_BOLD, 10);
            content.setNonStrokingColor(COLOR_WHITE, COLOR_WHITE, COLOR_WHITE);
            content.beginText();
            content.newLineAtOffset(colId + 9, y - 6);
            content.showText("Customer ID");
            content.endText();
            content.beginText();
            content.newLineAtOffset(colName + 9, y - 6);
            content.showText("Name");
            content.endText();
            content.beginText();
            content.newLineAtOffset(colContact + 9, y - 6);
            content.showText("Contact");
            content.endText();

            // Table body
            y -= 16;
            content.setFont(PDType1Font.HELVETICA, 10);
            int row = 0;
            for (CustomerEntity c : customers) {
                if (y < margin + 40) break;

                if (row % 2 == 0) {
                    content.setNonStrokingColor(COLOR_GRAY, COLOR_GRAY, COLOR_GRAY);
                    content.addRect(margin, y - 16, contentWidth, 16);
                    content.fill();
                }
                content.setNonStrokingColor(COLOR_DARK, COLOR_DARK, COLOR_DARK);

                String id = c.getCustId() != null ? c.getCustId() : "-";
                String name = c.getName() != null ? c.getName() : "-";
                String contact = c.getContact() != null ? c.getContact() : "-";

                float textY = y - 6;

                content.beginText();
                content.newLineAtOffset(colId + 9, textY);
                content.showText(id);
                content.endText();
                content.beginText();
                content.newLineAtOffset(colName + 9, textY);
                content.showText(truncate(name, 30));
                content.endText();
                content.beginText();
                content.newLineAtOffset(colContact + 9, textY);
                content.showText(contact);
                content.endText();

                y -= 16;
                row++;
            }

            // ================================
            // FOOTER SECTION
            // ================================
            y -= 25;
            content.moveTo(margin, y);
            content.lineTo(A4_WIDTH - margin, y);
            content.stroke();

            y -= 20;
            content.setFont(PDType1Font.HELVETICA, 9);
            content.setNonStrokingColor(COLOR_DARK, COLOR_DARK, COLOR_DARK);

            content.beginText();
            content.newLineAtOffset(margin, y);
            content.showText("Total Customers: " + customers.size());
            content.endText();

            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss"));
            float dateWidth = getTextWidth("Generated: " + dateTime, 9);
            content.beginText();
            content.newLineAtOffset(A4_WIDTH - margin - dateWidth, y);
            content.showText("Generated: " + dateTime);
            content.endText();

            y -= 15;
            content.setFont(PDType1Font.HELVETICA, 8);
            content.setNonStrokingColor(COLOR_GRAY, COLOR_GRAY, COLOR_GRAY);
            float devWidth = getTextWidth(DEVELOPED_BY, 8);
            content.beginText();
            content.newLineAtOffset((A4_WIDTH - devWidth) / 2, y);
            content.showText(DEVELOPED_BY);
            content.endText();

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
            float contentWidth = A4_WIDTH - (2 * margin);
            float y = A4_HEIGHT - margin;

            // ================================
            // HEADER SECTION (Centered)
            // ================================
            content.setFont(PDType1Font.HELVETICA_BOLD, 18);
            content.setNonStrokingColor(COLOR_DARK, COLOR_DARK, COLOR_DARK);
            float titleWidth = getTextWidth(COMPANY_NAME, 18);
            content.beginText();
            content.newLineAtOffset((A4_WIDTH - titleWidth) / 2, y);
            content.showText(COMPANY_NAME);
            content.endText();

            y -= 18;
            content.setFont(PDType1Font.HELVETICA, 10);
            content.setNonStrokingColor(COLOR_GRAY, COLOR_GRAY, COLOR_GRAY);
            float taglineWidth = getTextWidth(COMPANY_TAGLINE, 10);
            content.beginText();
            content.newLineAtOffset((A4_WIDTH - taglineWidth) / 2, y);
            content.showText(COMPANY_TAGLINE);
            content.endText();

            y -= 16;
            content.setFont(PDType1Font.HELVETICA, 9);
            content.setNonStrokingColor(COLOR_DARK, COLOR_DARK, COLOR_DARK);
            float addrWidth = getTextWidth(COMPANY_ADDRESS, 9);
            content.beginText();
            content.newLineAtOffset((A4_WIDTH - addrWidth) / 2, y);
            content.showText(COMPANY_ADDRESS);
            content.endText();

            y -= 14;
            float phoneWidth = getTextWidth(COMPANY_PHONE, 9);
            content.beginText();
            content.newLineAtOffset((A4_WIDTH - phoneWidth) / 2, y);
            content.showText(COMPANY_PHONE);
            content.endText();

            // Divider line
            y -= 18;
            content.setLineWidth(1f);
            content.setStrokingColor(COLOR_DARK, COLOR_DARK, COLOR_DARK);
            content.moveTo(margin, y);
            content.lineTo(A4_WIDTH - margin, y);
            content.stroke();

            // ================================
            // REPORT TITLE SECTION (Centered)
            // ================================
            y -= 30;
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            float reportTitleWidth = getTextWidth("PRODUCT REPORT", 14);
            content.beginText();
            content.newLineAtOffset((A4_WIDTH - reportTitleWidth) / 2, y);
            content.showText("PRODUCT REPORT");
            content.endText();

            // Thin separator line
            y -= 12;
            content.setLineWidth(0.5f);
            content.moveTo(margin + 50, y);
            content.lineTo(A4_WIDTH - margin - 50, y);
            content.stroke();

            // ================================
            // TABLE SECTION
            // ================================
            y -= 25;
            float colId = margin + 10;
            float colName = margin + 80;
            float colPrice = margin + 250;
            float colStock = margin + 350;

            // Table header (Black background, White text)
            content.setNonStrokingColor(COLOR_BLACK, COLOR_BLACK, COLOR_BLACK);
            content.addRect(margin, y - 16, contentWidth, 16);
            content.fill();

            content.setFont(PDType1Font.HELVETICA_BOLD, 10);
            content.setNonStrokingColor(COLOR_WHITE, COLOR_WHITE, COLOR_WHITE);
            content.beginText();
            content.newLineAtOffset(colId + 9, y - 6);
            content.showText("Product ID");
            content.endText();
            content.beginText();
            content.newLineAtOffset(colName + 9, y - 6);
            content.showText("Product Name");
            content.endText();
            content.beginText();
            content.newLineAtOffset(colPrice + 9, y - 6);
            content.showText("Price");
            content.endText();
            content.beginText();
            content.newLineAtOffset(colStock + 9, y - 6);
            content.showText("Stock");
            content.endText();

            // Table body
            y -= 16;
            content.setFont(PDType1Font.HELVETICA, 10);
            int row = 0;
            int lowStockCount = 0;
            for (ProductEntity p : products) {
                if (y < margin + 40) break;

                if (row % 2 == 0) {
                    content.setNonStrokingColor(COLOR_GRAY, COLOR_GRAY, COLOR_GRAY);
                    content.addRect(margin, y - 16, contentWidth, 16);
                    content.fill();
                }

                if (p.getQty() < 10) {
                    content.setNonStrokingColor(COLOR_BLACK, COLOR_BLACK, COLOR_BLACK);
                    lowStockCount++;
                } else {
                    content.setNonStrokingColor(COLOR_DARK, COLOR_DARK, COLOR_DARK);
                }

                String id = p.getId() != null ? p.getId() : "-";
                String name = p.getName() != null ? p.getName() : "-";
                String price = String.format("%,.2f", p.getPrice());
                String stock = String.valueOf(p.getQty());

                float textY = y - 6;

                content.beginText();
                content.newLineAtOffset(colId + 9, textY);
                content.showText(id);
                content.endText();
                content.beginText();
                content.newLineAtOffset(colName + 9, textY);
                content.showText(truncate(name, 30));
                content.endText();
                content.beginText();
                content.newLineAtOffset(colPrice + 9, textY);
                content.showText(price);
                content.endText();
                content.beginText();
                content.newLineAtOffset(colStock + 9, textY);
                content.showText(stock);
                content.endText();

                y -= 16;
                row++;
            }

            // ================================
            // FOOTER SECTION
            // ================================
            y -= 25;
            content.moveTo(margin, y);
            content.lineTo(A4_WIDTH - margin, y);
            content.stroke();

            y -= 20;
            content.setFont(PDType1Font.HELVETICA, 9);
            content.setNonStrokingColor(COLOR_DARK, COLOR_DARK, COLOR_DARK);

            content.beginText();
            content.newLineAtOffset(margin, y);
            content.showText("Total Products: " + products.size());
            content.endText();

            content.beginText();
            content.newLineAtOffset(margin + 120, y);
            content.showText("Low Stock: " + lowStockCount);
            content.endText();

            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss"));
            float dateWidth = getTextWidth("Generated: " + dateTime, 9);
            content.beginText();
            content.newLineAtOffset(A4_WIDTH - margin - dateWidth, y);
            content.showText("Generated: " + dateTime);
            content.endText();

            y -= 15;
            content.setFont(PDType1Font.HELVETICA, 8);
            content.setNonStrokingColor(COLOR_GRAY, COLOR_GRAY, COLOR_GRAY);
            float devWidth = getTextWidth(DEVELOPED_BY, 8);
            content.beginText();
            content.newLineAtOffset((A4_WIDTH - devWidth) / 2, y);
            content.showText(DEVELOPED_BY);
            content.endText();

            content.close();
            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================================
    // 3. ORDER BILL (80mm Thermal)
    // ================================
    public static byte[] generateOrderBill(OrderEntity order, List<OrderProductDetailsEntity> details) {
        return generateOrderBill(order, details, 0.0, 0.0, 0.0, 0.0);
    }

    public static byte[] generateOrderBill(OrderEntity order,
                               List<OrderProductDetailsEntity> details,
                               double taxPercent,
                               double discountPercent,
                               double amountPaid,
                               double balance) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            float paperWidth = 80f * MM_TO_POINTS;
            PDPage page = new PDPage(new PDRectangle(paperWidth, 850));
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);

            float left = 12f;
            float y = page.getMediaBox().getHeight() - 30;

            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            float titleWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(COMPANY_NAME) / 1000f) * 14;
            content.beginText();
            content.newLineAtOffset((paperWidth - titleWidth) / 2, y);
            content.showText(COMPANY_NAME);
            content.endText();

            y -= 18;
            content.setFont(PDType1Font.HELVETICA, 8);
            float addrWidth = (PDType1Font.HELVETICA.getStringWidth(COMPANY_ADDRESS) / 1000f) * 8;
            content.beginText();
            content.newLineAtOffset((paperWidth - addrWidth) / 2, y);
            content.showText(COMPANY_ADDRESS);
            content.endText();

            y -= 12;
            float phoneWidth = (PDType1Font.HELVETICA.getStringWidth(COMPANY_PHONE) / 1000f) * 8;
            content.beginText();
            content.newLineAtOffset((paperWidth - phoneWidth) / 2, y);
            content.showText(COMPANY_PHONE);
            content.endText();

            y -= 18;
            content.setLineDashPattern(new float[]{3, 2}, 0);
            content.moveTo(left, y);
            content.lineTo(paperWidth - left, y);
            content.stroke();
            content.setLineDashPattern(new float[]{}, 0);

            y -= 16;
            content.setFont(PDType1Font.HELVETICA_BOLD, 11);
            String invoiceTitle = "INVOICE / CASH MEMO";
            float invWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(invoiceTitle) / 1000f) * 11;
            content.beginText();
            content.newLineAtOffset((paperWidth - invWidth) / 2, y);
            content.showText(invoiceTitle);
            content.endText();

            y -= 18;
            content.setFont(PDType1Font.HELVETICA, 8);
            String invoiceNo = order.getOrderId() != null ? order.getOrderId() : "-";
            content.beginText();
            content.newLineAtOffset(left, y);
            content.showText("Bill No: " + invoiceNo);
            content.endText();
            y -= 14;

            String dateStr = order.getDate() != null 
                ? order.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
            content.beginText();
            content.newLineAtOffset(left, y);
            content.showText("Date  : " + dateStr);
            content.endText();
            y -= 14;

            String custName = order.getCustomer() != null && order.getCustomer().getName() != null 
                ? order.getCustomer().getName() : "Walk-in Customer";
            content.beginText();
            content.newLineAtOffset(left, y);
            content.showText("Cust  : " + truncate(custName, 28));
            content.endText();
            y -= 18;

            float colItemX = left + 14;
            float rightQty = left + 132;
            float rightRate = left + 168;
            float rightAmt = left + 205;

            content.setFont(PDType1Font.HELVETICA_BOLD, 8);
            content.beginText();
            content.newLineAtOffset(left, y);
            content.showText("#");
            content.endText();
            content.beginText();
            content.newLineAtOffset(colItemX, y);
            content.showText("Item");
            content.endText();

            float qtyWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth("Qty") / 1000f) * 8;
            content.beginText();
            content.newLineAtOffset(rightQty - qtyWidth, y);
            content.showText("Qty");
            content.endText();

            float rateWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth("Rate") / 1000f) * 8;
            content.beginText();
            content.newLineAtOffset(rightRate - rateWidth, y);
            content.showText("Rate");
            content.endText();

            float amtWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth("Amount") / 1000f) * 8;
            content.beginText();
            content.newLineAtOffset(rightAmt - amtWidth, y);
            content.showText("Amount");
            content.endText();

            y -= 10;
            content.setLineDashPattern(new float[]{3, 2}, 0);
            content.moveTo(left, y);
            content.lineTo(paperWidth - left, y);
            content.stroke();
            content.setLineDashPattern(new float[]{}, 0);

            y -= 14;
            content.setFont(PDType1Font.HELVETICA, 8);
            double subTotal = 0.0;
            int serial = 1;

            for (OrderProductDetailsEntity d : details) {
                if (y < 100) break;

                double lineTotal = d.getQuantity() * d.getUnitPrice();
                subTotal += lineTotal;

                content.beginText();
                content.newLineAtOffset(left, y);
                content.showText(String.valueOf(serial++));
                content.endText();

                String prodName = d.getProduct() != null && d.getProduct().getName() != null 
                    ? d.getProduct().getName() : "-";
                content.beginText();
                content.newLineAtOffset(colItemX, y);
                content.showText(truncate(prodName, 22));
                content.endText();

                String qty = String.valueOf(d.getQuantity());
                qtyWidth = (PDType1Font.HELVETICA.getStringWidth(qty) / 1000f) * 8;
                content.beginText();
                content.newLineAtOffset(rightQty - qtyWidth, y);
                content.showText(qty);
                content.endText();

                String rate = String.format("%.2f", d.getUnitPrice());
                rateWidth = (PDType1Font.HELVETICA.getStringWidth(rate) / 1000f) * 8;
                content.beginText();
                content.newLineAtOffset(rightRate - rateWidth, y);
                content.showText(rate);
                content.endText();

                String amt = String.format("%.2f", lineTotal);
                amtWidth = (PDType1Font.HELVETICA.getStringWidth(amt) / 1000f) * 8;
                content.beginText();
                content.newLineAtOffset(rightAmt - amtWidth, y);
                content.showText(amt);
                content.endText();

                y -= 15;
            }

            y -= 8;
            content.setLineDashPattern(new float[]{3, 2}, 0);
            content.moveTo(left, y);
            content.lineTo(paperWidth - left, y);
            content.stroke();
            content.setLineDashPattern(new float[]{}, 0);

            y -= 18;
            content.setFont(PDType1Font.HELVETICA, 9);
            content.beginText();
            content.newLineAtOffset(left, y);
            content.showText("Sub Total : LKR " + String.format("%.2f", subTotal));
            content.endText();
            y -= 16;

            double discountAmount = subTotal * (discountPercent / 100.0);
            double afterDiscount = subTotal - discountAmount;
            content.beginText();
            content.newLineAtOffset(left, y);
            content.showText("Discount (" + String.format("%.0f", discountPercent) + "%) : LKR " + String.format("%.2f", discountAmount));
            content.endText();
            y -= 16;

            double taxAmount = afterDiscount * (taxPercent / 100.0);
            double netTotal = afterDiscount + taxAmount;
            content.beginText();
            content.newLineAtOffset(left, y);
            content.showText("Tax (" + String.format("%.0f", taxPercent) + "%) : LKR " + String.format("%.2f", taxAmount));
            content.endText();
            y -= 20;

            content.setLineDashPattern(new float[]{3, 2}, 0);
            content.moveTo(left, y);
            content.lineTo(paperWidth - left, y);
            content.stroke();
            content.setLineDashPattern(new float[]{}, 0);

            y -= 16;
            content.setFont(PDType1Font.HELVETICA_BOLD, 11);
            content.beginText();
            content.newLineAtOffset(left, y);
            content.showText("NET TOTAL : LKR " + String.format("%.2f", netTotal));
            content.endText();
            y -= 22;

            content.setFont(PDType1Font.HELVETICA, 9);
            content.beginText();
            content.newLineAtOffset(left, y);
            content.showText("Paid : LKR " + String.format("%.2f", amountPaid));
            content.endText();
            y -= 16;

            double balanceDue = amountPaid - netTotal;
            content.beginText();
            content.newLineAtOffset(left, y);
            content.showText("Balance : LKR " + String.format("%.2f", balanceDue));
            content.endText();
            y -= 24;

            content.setLineDashPattern(new float[]{3, 2}, 0);
            content.moveTo(left, y);
            content.lineTo(paperWidth - left, y);
            content.stroke();
            content.setLineDashPattern(new float[]{}, 0);

            y -= 16;
            content.setFont(PDType1Font.HELVETICA, 7);
            String generated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            float genWidth = (PDType1Font.HELVETICA.getStringWidth("Generated : " + generated) / 1000f) * 7;
            content.beginText();
            content.newLineAtOffset((paperWidth - genWidth) / 2, y);
            content.showText("Generated : " + generated);
            content.endText();
            y -= 16;

            content.setFont(PDType1Font.HELVETICA_BOLD, 10);
            String thankYou = "Thank You! Visit Again";
            float thankWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(thankYou) / 1000f) * 10;
            content.beginText();
            content.newLineAtOffset((paperWidth - thankWidth) / 2, y);
            content.showText(thankYou);
            content.endText();
            y -= 12;

            content.setFont(PDType1Font.HELVETICA, 8);
            content.beginText();
            content.newLineAtOffset(left, y);
            content.showText("Signature : _____________");
            content.endText();

            content.close();
            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static float getTextWidth(String text, float fontSize) throws IOException {
        return (PDType1Font.HELVETICA.getStringWidth(text) / 1000f) * fontSize;
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max - 1) + "…" : text;
    }
}