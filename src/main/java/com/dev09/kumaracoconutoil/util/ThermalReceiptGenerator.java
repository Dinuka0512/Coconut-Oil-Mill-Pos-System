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
 * <p>The layout uses the printer's default single-width font, so bills and
 * reports print as a normal, clear cash memo instead of oversized double-height
 * text. 42 columns fit 80mm paper with a small margin. The resulting byte stream
 * is sent to the receipt printer by {@link ThermalPrinter}.</p>
 */
final class ThermalReceiptGenerator {

    /** Font A at normal size fits 42 columns across 80mm paper. */
    private static final int COLUMNS = 42;
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
            divider(p);
            breakdown(p, model);
            totalRow(p, "TOTAL (LKR)", value(model, "total"));
            divider(p);
            meta(p, "Payment", value(model, "paymentMethod"));
            meta(p, "Paid", value(model, "amountPaid"));
            if (flag(model, "hasDuePaid")) {
                meta(p, "Due Paid", value(model, "duePaid"));
            }
            if (flag(model, "hasNewDue")) {
                meta(p, "New Due", value(model, "newDue"));
            }
            totalRow(p, "BALANCE", value(model, "balanceText"));
            if (flag(model, "hasTotalDue")) {
                totalRow(p, "TOTAL DUE", value(model, "totalDue"));
            }
            footer(p, model);
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
            divider(p);
            breakdown(p, model);
            totalRow(p, "TOTAL (LKR)", value(model, "total"));
            divider(p);
            meta(p, "Type", "Quotation");
            meta(p, "Validity", "Prices valid on issue date");
            footer(p, model, "**Condition applied", "**Valid only for 24 hours");
        });
    }

    static byte[] renderCustomerReport(Map<String, Object> model) {
        return render(p -> {
            reportHeader(p, model, value(model, "subtitle"));
            meta(p, "Date", value(model, "generatedAt"));
            meta(p, "Customers", value(model, "count"));
            divider(p);
            p.writeLF(bold(), customerColumns("ID", "Name", "Contact", "Due"));
            divider(p);
            List<Map<String, Object>> rows = rows(model);
            if (rows.isEmpty()) {
                p.writeLF(normal(), "No customers found.");
            }
            for (Map<String, Object> row : rows) {
                p.writeLF(normal(), customerColumns(value(row, "id"), value(row, "name"),
                        value(row, "contact"), value(row, "due")));
            }
            divider(p);
            totalRow(p, "TOTAL DUE", value(model, "totalDue"));
            footer(p, model);
        });
    }

    static byte[] renderSupplierReport(Map<String, Object> model) {
        return render(p -> {
            reportHeader(p, model, value(model, "subtitle"));
            meta(p, "Date", value(model, "generatedAt"));
            meta(p, "Suppliers", value(model, "count"));
            divider(p);
            p.writeLF(bold(), supplierColumns("ID", "Name", "Contact"));
            divider(p);
            List<Map<String, Object>> rows = rows(model);
            if (rows.isEmpty()) {
                p.writeLF(normal(), "No suppliers found.");
            }
            for (Map<String, Object> row : rows) {
                p.writeLF(normal(), supplierColumns(value(row, "id"), value(row, "name"),
                        value(row, "contact")));
            }
            divider(p);
            totalRow(p, "SUPPLIERS", value(model, "count"));
            footer(p, model);
        });
    }

    static byte[] renderDueReport(Map<String, Object> model) {
        return render(p -> {
            reportHeader(p, model, value(model, "subtitle"));
            meta(p, "Date", value(model, "generatedAt"));
            meta(p, "Customers", value(model, "count"));
            divider(p);
            p.writeLF(bold(), customerColumns("ID", "Name", "Contact", "Due"));
            divider(p);
            List<Map<String, Object>> rows = rows(model);
            if (rows.isEmpty()) {
                p.writeLF(normal(), "No customers found.");
            }
            for (Map<String, Object> row : rows) {
                p.writeLF(normal(), customerColumns(value(row, "id"), value(row, "name"),
                        value(row, "contact"), value(row, "due")));
            }
            divider(p);
            totalRow(p, "TOTAL DUE", value(model, "totalDue"));
            footer(p, model);
        });
    }

    static byte[] renderProductReport(Map<String, Object> model) {
        return render(p -> {
            reportHeader(p, model, value(model, "subtitle"));
            meta(p, "Date", value(model, "generatedAt"));
            meta(p, "Products", value(model, "count"));
            divider(p);
            p.writeLF(bold(), productColumns("Code", "Name", "Price", "Qty"));
            divider(p);
            List<Map<String, Object>> rows = rows(model);
            if (rows.isEmpty()) {
                p.writeLF(normal(), "No products found.");
            }
            for (Map<String, Object> row : rows) {
                p.writeLF(normal(), productColumns(value(row, "itemCode"), value(row, "name"),
                        value(row, "price"), value(row, "qty")));
            }
            divider(p);
            totalRow(p, "LOW STOCK", value(model, "lowStock"));
            footer(p, model);
        });
    }

    static byte[] renderSalesSummary(Map<String, Object> model) {
        return render(p -> {
            reportHeader(p, model, value(model, "subtitle"));
            meta(p, "Date", value(model, "generatedAt"));
            meta(p, "Products Sold", value(model, "soldQty"));
            divider(p);
            p.writeLF(bold(), salesColumns("Code", "Name", "Qty", "Total"));
            divider(p);
            List<Map<String, Object>> rows = rows(model);
            if (rows.isEmpty()) {
                p.writeLF(normal(), "No sales found for this date.");
            }
            for (Map<String, Object> row : rows) {
                p.writeLF(normal(), salesColumns(value(row, "code"), value(row, "name"),
                        value(row, "qty"), value(row, "total")));
            }
            divider(p);
            totalRow(p, "TOTAL SALES", value(model, "totalAmount"));
            footer(p, model);
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
            centerOnPaper(printer);
            body.write(printer);
            printer.feed(2);
            bottomMargin(printer);
            printer.cut(EscPos.CutMode.PART);
            printer.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to build 80mm ESC/POS receipt", e);
        }
    }

    /**
     * Centers the 42-column receipt on 80mm paper with equal 36-dot (3 character)
     * left and right margins. Printers that do not support these commands simply
     * ignore them, so the bill still prints normally.
     */
    private static void centerOnPaper(EscPos printer) throws IOException {
        // GS L nL nH: left margin = 36 dots (3 characters).
        byte[] leftMargin = {0x1D, 0x4C, 0x24, 0x00};
        // GS W nL nH: print width = 504 dots (42 characters) from the left margin.
        byte[] printWidth = {0x1D, 0x57, (byte) 0xF8, 0x01};
        printer.write(leftMargin, 0, leftMargin.length);
        printer.write(printWidth, 0, printWidth.length);
    }

    /** Extra 250-dot gap under the bill before the cut, so text clears the tear edge. */
    private static void bottomMargin(EscPos printer) throws IOException {
        byte[] feedDots = {0x1B, 0x4A, (byte) 0xFA}; // ESC J 250
        printer.write(feedDots, 0, feedDots.length);
    }

    private static Style normal() {
        return defaultSize(new Style());
    }

    private static Style bold() {
        return defaultSize(new Style().setBold(true));
    }

    private static Style centered() {
        return defaultSize(new Style().setJustification(Justification.Center));
    }

    private static Style centeredBold() {
        return defaultSize(new Style().setBold(true).setJustification(Justification.Center));
    }

    /** Single-width text: the standard font used on cash memos. */
    private static Style defaultSize(Style style) {
        return style.setFontSize(Style.FontSize._1, Style.FontSize._1);
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
    }

    private static void totalRow(EscPos p, String label, String amount) throws IOException {
        String safeAmount = truncate(amount, 12);
        p.writeLF(bold(), left(label, COLUMNS - safeAmount.length() - 1) + " " + safeAmount);
    }

    private static void footer(EscPos p, Map<String, Object> model, String... notes) throws IOException {
        p.feed(1);
        if (notes != null) {
            for (String note : notes) {
                if (note != null && !note.isBlank()) {
                    p.writeLF(centered(), truncate(note, COLUMNS));
                }
            }
        }
        p.writeLF(centered(), "Thank you for your business!");
        p.writeLF(centered(), truncate(value(model, "footerCompany"), COLUMNS));
    }

    // ------------------------------------------------------------------
    // Fixed-width receipt tables (42 columns, normal-size characters)
    // ------------------------------------------------------------------

    private static void itemRows(EscPos p, List<Map<String, Object>> rows, String descriptionKey)
            throws IOException {
        p.writeLF(bold(), "# " + left("Item", COLUMNS - 2));
        divider(p);
        if (rows.isEmpty()) {
            p.writeLF(normal(), "No items found.");
            return;
        }
        for (Map<String, Object> row : rows) {
            p.writeLF(normal(), left(value(row, "index"), 2) + " "
                    + left(value(row, descriptionKey), COLUMNS - 3));
            String qtyRate = value(row, "qty") + " x " + value(row, "rate");
            p.writeLF(normal(), "   " + right(qtyRate, 17) + " "
                    + right(value(row, "amount"), COLUMNS - 21));
        }
    }

    private static String customerColumns(String id, String name, String contact, String due) {
        return left(id, 6) + " " + left(name, 14) + " " + left(contact, 10) + " " + right(due, 9);
    }

    private static String supplierColumns(String id, String name, String contact) {
        return left(id, 8) + " " + left(name, 20) + " " + right(contact, 11);
    }

    private static String productColumns(String code, String name, String price, String qty) {
        return left(code, 8) + " " + left(name, 16) + " " + right(price, 8) + " " + right(qty, 7);
    }

    private static String salesColumns(String code, String name, String qty, String total) {
        return left(code, 7) + " " + left(name, 15) + " " + right(qty, 6) + " " + right(total, 10);
    }

    private static void twoColumns(EscPos p, String label, String amount) throws IOException {
        String safeAmount = truncate(amount, COLUMNS - 4);
        int labelWidth = Math.max(1, COLUMNS - safeAmount.length() - 1);
        String safeLabel = left(label, labelWidth);
        p.writeLF(normal(), safeLabel + " " + right(safeAmount, COLUMNS - labelWidth - 1));
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
