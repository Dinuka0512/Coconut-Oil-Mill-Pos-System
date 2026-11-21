package com.dev09.kumaracoconutoil.util;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst.Justification;
import com.github.anastaciocintra.escpos.Style;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Builds the 80mm receipt content as ESC/POS commands using escpos-coffee.
 *
 * <p>The data models are prepared by {@link PdfGenerator}; this class only
 * turns them into a 24-column double-size thermal receipt so the letters are
 * easy to read on paper. The resulting byte stream is sent to the receipt
 * printer by {@link ThermalPrinter}.</p>
 */
final class ThermalReceiptGenerator {

    /** Double-width font fits 24 characters across 80mm paper. */
    private static final int COLUMNS = 24;
    private static final Style.FontSize LARGE = Style.FontSize._2;
    private static final String DIVIDER = "-".repeat(COLUMNS);

    private ThermalReceiptGenerator() {
    }

    static byte[] renderOrderBill(Map<String, Object> model) {
        return render(p -> {
            header(p, model, "INVOICE", "Sales invoice " + value(model, "orderId"));
            meta(p, "Date", value(model, "orderDateTime"));
            meta(p, "Customer", value(model, "customerName"));
            if (flag(model, "hasContact")) {
                meta(p, "Contact", value(model, "customerContact"));
            }
            divider(p);
            itemRows(p, rows(model), "name");
            breakdown(p, model);
            totalLine(p, "TOTAL (LKR) " + value(model, "total"));
            divider(p);
            meta(p, "Payment", value(model, "paymentMethod"));
            meta(p, "Paid", value(model, "amountPaid"));
            if (flag(model, "hasDuePaid")) {
                meta(p, "Due Paid", value(model, "duePaid"));
            }
            totalLine(p, "BALANCE" + value(model, "balanceSuffix") + " " + value(model, "balanceText"));
            footer(p, model, "** Valid for 3 Days Only.");
        });
    }

    static byte[] renderQuotation(Map<String, Object> model) {
        return render(p -> {
            header(p, model, "QUOTATION", value(model, "subtitle"));
            meta(p, "Date", value(model, "quotationDateTime"));
            meta(p, "Customer", value(model, "customerName"));
            if (flag(model, "hasContact")) {
                meta(p, "Contact", value(model, "customerContact"));
            }
            divider(p);
            itemRows(p, rows(model), "description");
            breakdown(p, model);
            totalLine(p, "TOTAL (LKR) " + value(model, "total"));
            divider(p);
            meta(p, "Type", "Quotation");
            meta(p, "Validity", "Prices valid on issue date");
            footer(p, model, "**Condition applied");
        });
    }

    static byte[] renderCustomerReport(Map<String, Object> model) {
        return render(p -> {
            reportHeader(p, model, value(model, "subtitle"));
            meta(p, "Date", value(model, "generatedAt"));
            meta(p, "Customers", value(model, "count"));
            divider(p);
            fourColumnHeader(p, "ID", "Name", "Contact", "Due");
            List<Map<String, Object>> rows = rows(model);
            if (rows.isEmpty()) {
                p.writeLF(normal(), "No customers found.");
            }
            for (Map<String, Object> row : rows) {
                fourColumns(p, value(row, "id"), value(row, "name"), value(row, "contact"), value(row, "due"));
            }
            divider(p);
            totalLine(p, "TOTAL DUE " + value(model, "totalDue"));
            footer(p, model, null);
        });
    }

    static byte[] renderSupplierReport(Map<String, Object> model) {
        return render(p -> {
            reportHeader(p, model, value(model, "subtitle"));
            meta(p, "Date", value(model, "generatedAt"));
            meta(p, "Suppliers", value(model, "count"));
            divider(p);
            threeColumnHeader(p, "ID", "Name", "Contact");
            List<Map<String, Object>> rows = rows(model);
            if (rows.isEmpty()) {
                p.writeLF(normal(), "No suppliers found.");
            }
            for (Map<String, Object> row : rows) {
                threeColumns(p, value(row, "id"), value(row, "name"), value(row, "contact"));
            }
            divider(p);
            totalLine(p, "SUPPLIERS " + value(model, "count"));
            footer(p, model, null);
        });
    }

    static byte[] renderDueReport(Map<String, Object> model) {
        return render(p -> {
            reportHeader(p, model, value(model, "subtitle"));
            meta(p, "Date", value(model, "generatedAt"));
            meta(p, "Customers", value(model, "count"));
            divider(p);
            fourColumnHeader(p, "ID", "Name", "Contact", "Due");
            List<Map<String, Object>> rows = rows(model);
            if (rows.isEmpty()) {
                p.writeLF(normal(), "No customers found.");
            }
            for (Map<String, Object> row : rows) {
                fourColumns(p, value(row, "id"), value(row, "name"), value(row, "contact"), value(row, "due"));
            }
            divider(p);
            totalLine(p, "TOTAL DUE " + value(model, "totalDue"));
            footer(p, model, null);
        });
    }

    static byte[] renderProductReport(Map<String, Object> model) {
        return render(p -> {
            reportHeader(p, model, value(model, "subtitle"));
            meta(p, "Date", value(model, "generatedAt"));
            meta(p, "Products", value(model, "count"));
            divider(p);
            fourColumnHeader(p, "Code", "Name", "Price", "Qty");
            List<Map<String, Object>> rows = rows(model);
            if (rows.isEmpty()) {
                p.writeLF(normal(), "No products found.");
            }
            for (Map<String, Object> row : rows) {
                fourColumns(p, value(row, "itemCode"), value(row, "name"), value(row, "price"), value(row, "qty"));
            }
            divider(p);
            totalLine(p, "LOW STOCK " + value(model, "lowStock"));
            footer(p, model, null);
        });
    }

    static byte[] renderSalesSummary(Map<String, Object> model) {
        return render(p -> {
            reportHeader(p, model, value(model, "subtitle"));
            meta(p, "Date", value(model, "generatedAt"));
            meta(p, "Products Sold", value(model, "soldQty"));
            divider(p);
            fourColumnHeader(p, "Code", "Name", "Qty", "Total");
            List<Map<String, Object>> rows = rows(model);
            if (rows.isEmpty()) {
                p.writeLF(normal(), "No sales found for this date.");
            }
            for (Map<String, Object> row : rows) {
                fourColumns(p, value(row, "code"), value(row, "name"), value(row, "qty"), value(row, "total"));
            }
            divider(p);
            totalLine(p, "TOTAL SALES " + value(model, "totalAmount"));
            footer(p, model, null);
        });
    }

    // ------------------------------------------------------------------
    // ESC/POS document building
    // ------------------------------------------------------------------

    @FunctionalInterface
    private interface ReceiptBody {
        void write(EscPos printer) throws IOException;
    }

    private static byte[] render(ReceiptBody body) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            EscPos printer = new EscPos(out);
            printer.initializePrinter();
            printer.setCharacterCodeTable(EscPos.CharacterCodeTable.WPC1252);
            body.write(printer);
            printer.feed(3);
            printer.cut(EscPos.CutMode.PART);
            printer.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to build 80mm ESC/POS receipt", e);
        }
    }

    private static Style normal() {
        return large(new Style());
    }

    private static Style bold() {
        return large(new Style().setBold(true));
    }

    private static Style centered() {
        return large(new Style().setJustification(Justification.Center));
    }

    private static Style centeredBold() {
        return large(new Style().setBold(true).setJustification(Justification.Center));
    }

    private static Style rightAligned() {
        return large(new Style().setBold(true).setJustification(Justification.Right));
    }

    private static Style large(Style style) {
        return style.setFontSize(LARGE, LARGE);
    }

    private static void header(EscPos p, Map<String, Object> model, String title, String subtitle)
            throws IOException {
        p.writeLF(centeredBold(), truncate(value(model, "companyName"), COLUMNS));
        p.writeLF(centered(), truncate(value(model, "companyTagline"), COLUMNS));
        p.writeLF(centered(), truncate(value(model, "companyAddress"), COLUMNS));
        p.writeLF(centered(), truncate(value(model, "companyPhone"), COLUMNS));
        divider(p);
        p.writeLF(centeredBold(), truncate(title, COLUMNS));
        p.writeLF(centered(), truncate(subtitle, COLUMNS));
        divider(p);
    }

    private static void reportHeader(EscPos p, Map<String, Object> model, String subtitle) throws IOException {
        header(p, model, value(model, "reportTitle"), subtitle);
    }

    private static void divider(EscPos p) throws IOException {
        p.writeLF(normal(), DIVIDER);
    }

    private static void meta(EscPos p, String label, String text) throws IOException {
        p.writeLF(normal(), truncate(label + ": " + text, COLUMNS));
    }

    private static void breakdown(EscPos p, Map<String, Object> model) throws IOException {
        twoColumns(p, "Sub Total", value(model, "subTotal"));
        twoColumns(p, "Discount (" + value(model, "discountPercent") + "%)", value(model, "discount"));
        twoColumns(p, "Tax (" + value(model, "taxRate") + "%)", value(model, "tax"));
        divider(p);
    }

    private static void totalLine(EscPos p, String text) throws IOException {
        p.writeLF(rightAligned(), truncate(text, COLUMNS));
    }

    private static void footer(EscPos p, Map<String, Object> model, String note) throws IOException {
        p.feed(1);
        if (note != null && !note.isBlank()) {
            p.writeLF(centered(), truncate(note, COLUMNS));
        }
        p.writeLF(centered(), "Thank you for your business!");
        p.writeLF(centered(), truncate(value(model, "footerCompany"), COLUMNS));
    }

    // ------------------------------------------------------------------
    // Fixed-width receipt tables (24 columns, double-size characters)
    // ------------------------------------------------------------------

    private static void itemRows(EscPos p, List<Map<String, Object>> rows, String descriptionKey)
            throws IOException {
        twoColumns(p, "# Item", "Qty Rate Amt");
        divider(p);
        if (rows.isEmpty()) {
            p.writeLF(normal(), "No items found.");
            return;
        }
        for (Map<String, Object> row : rows) {
            p.writeLF(normal(), left(value(row, "index"), 2) + " " + left(value(row, descriptionKey), 21));
            p.writeLF(normal(), "   " + right(value(row, "qty") + "x" + value(row, "rate"), 10)
                    + " " + right(value(row, "amount"), 10));
        }
    }

    private static void fourColumnHeader(EscPos p, String c1, String c2, String c3, String c4)
            throws IOException {
        p.writeLF(bold(), left(c1 + " " + c2, COLUMNS));
        p.writeLF(bold(), right(c3 + " " + c4, COLUMNS));
        divider(p);
    }

    private static void threeColumnHeader(EscPos p, String c1, String c2, String c3) throws IOException {
        p.writeLF(bold(), left(c1 + " " + c2, COLUMNS));
        p.writeLF(bold(), right(c3, COLUMNS));
        divider(p);
    }

    private static void threeColumns(EscPos p, String c1, String c2, String c3) throws IOException {
        p.writeLF(normal(), left(c1 + " " + c2, COLUMNS));
        p.writeLF(normal(), right(c3, COLUMNS));
    }

    private static void fourColumns(EscPos p, String c1, String c2, String c3, String c4) throws IOException {
        p.writeLF(normal(), left(c1 + " " + c2, COLUMNS));
        p.writeLF(normal(), right(c3 + " " + c4, COLUMNS));
    }

    private static void twoColumns(EscPos p, String label, String amount) throws IOException {
        String safeAmount = truncate(amount, COLUMNS - 4);
        int labelWidth = Math.max(1, COLUMNS - safeAmount.length() - 1);
        String safeLabel = left(label, labelWidth);
        String line = safeLabel + " " + right(safeAmount, COLUMNS - labelWidth - 1);
        p.writeLF(normal(), line);
    }

    private static String left(String text, int width) {
        String safe = truncate(text, width);
        return safe + " ".repeat(Math.max(0, width - safe.length()));
    }

    private static String right(String text, int width) {
        String safe = truncate(text, width);
        return " ".repeat(Math.max(0, width - safe.length())) + safe;
    }

    private static String truncate(String text, int maxLength) {
        String safe = text == null ? "-" : text.replace('\r', ' ').replace('\n', ' ');
        if (maxLength <= 0) {
            return "";
        }
        return safe.length() <= maxLength ? safe : safe.substring(0, maxLength);
    }

    // ------------------------------------------------------------------
    // Model access helpers
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> rows(Map<String, Object> model) {
        Object rows = model == null ? null : model.get("rows");
        return rows instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    private static String value(Map<String, Object> model, String key) {
        if (model == null) {
            return "-";
        }
        Object value = model.get(key);
        return value == null || String.valueOf(value).isBlank() ? "-" : String.valueOf(value);
    }

    private static boolean flag(Map<String, Object> model, String key) {
        return model != null && Boolean.TRUE.equals(model.get(key));
    }
}
