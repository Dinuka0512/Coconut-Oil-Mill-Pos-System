package com.dev09.kumaracoconutoil.util;

import com.dev09.kumaracoconutoil.entity.CustomerEntity;
import com.dev09.kumaracoconutoil.entity.OrderEntity;
import com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity;
import com.dev09.kumaracoconutoil.entity.ProductEntity;
import com.dev09.kumaracoconutoil.entity.SupplierEntity;
import com.dev09.kumaracoconutoil.view.tm.CartTM;
import com.dev09.kumaracoconutoil.view.tm.ProductSalesTM;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Prepares the data for every report and delegates rendering to the correct
 * engine for its output size:
 *
 * <ul>
 *   <li>80mm receipts are built as ESC/POS commands with escpos-coffee and sent
 *       to the thermal printer by {@link ThermalPrinter};</li>
 *   <li>A4 bills and reports are rendered from HTML templates to PDF by
 *       {@link HtmlReportRenderer}.</li>
 * </ul>
 *
 * <p>The public method signatures are intentionally unchanged so existing
 * controllers only need to choose how the returned bytes are delivered.</p>
 */
public class PdfGenerator {

    private static final String COMPANY_NAME = "DILSHAN OIL MILL";
    private static final String COMPANY_TAGLINE = "Your Trusted Partner in Coconut Products";
    private static final String COMPANY_ADDRESS = "309/a 5 Baddegoda Rd, Bandaragama 12530";
    private static final String COMPANY_PHONE = "Tel: 077-6518819 | 077-7307805";
    private static final String DEVELOPED_BY = "Developed by Dev09Solutions.com";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
    private static final DateTimeFormatter BILL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter BILL_DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static final String CUSTOMER_REPORT_A4 = "customer-report-a4.html";
    private static final String SUPPLIER_REPORT_A4 = "supplier-report-a4.html";
    private static final String PRODUCT_REPORT_A4 = "product-report-a4.html";
    private static final String DUE_REPORT_A4 = "due-report-a4.html";
    private static final String SALES_SUMMARY_A4 = "sales-summary-a4.html";
    private static final String INVOICE_A4_BILL = "invoice-a4-bill.html";
    private static final String INVOICE_A4 = "invoice-a4.html";
    private static final String QUOTATION_A4 = "quotation-a4.html";

    private PdfGenerator() {
    }

    // ================================================================
    // Customer reports
    // ================================================================

    public static byte[] CustomerReportThermal(List<CustomerEntity> customers) {
        List<CustomerEntity> safeCustomers = customers == null ? List.of() : customers;
        Map<String, Object> model = baseModel("CUSTOMER REPORT");
        model.put("subtitle", "Customer directory");
        List<Map<String, Object>> rows = new ArrayList<>();
        double totalDue = 0;

        for (CustomerEntity customer : safeCustomers) {
            totalDue += customer.getDueAmount();
            Map<String, Object> row = customerRow(customer, rows.size() + 1, false);
            row.put("name", truncate(customer.getName(), 17));
            rows.add(row);
        }

        model.put("rows", rows);
        model.put("count", rows.size());
        model.put("totalDue", plainMoney(totalDue));

        return ThermalReceiptGenerator.renderCustomerReport(model);
    }

    public static byte[] CustomerReportA4Pro(List<CustomerEntity> customers) {
        List<CustomerEntity> safeCustomers = customers == null ? List.of() : customers;
        Map<String, Object> model = baseModel("CUSTOMER REPORT");
        model.put("subtitle", "Customer directory");
        List<Map<String, Object>> rows = new ArrayList<>();
        double totalDue = 0;

        for (CustomerEntity customer : safeCustomers) {
            totalDue += customer.getDueAmount();
            rows.add(customerRow(customer, rows.size() + 1, true));
        }

        model.put("rows", rows);
        model.put("count", rows.size());
        model.put("totalDue", money(totalDue));

        return HtmlReportRenderer.render(CUSTOMER_REPORT_A4, model,
                HtmlReportRenderer.PageSpec.a4());
    }

    // ================================================================
    // Supplier reports
    // ================================================================

    public static byte[] SupplierReportThermal(List<SupplierEntity> suppliers) {
        List<SupplierEntity> safeSuppliers = suppliers == null ? List.of() : suppliers;
        Map<String, Object> model = baseModel("SUPPLIER REPORT");
        model.put("subtitle", "Supplier directory");
        List<Map<String, Object>> rows = new ArrayList<>();

        for (SupplierEntity supplier : safeSuppliers) {
            rows.add(supplierRow(supplier, rows.size() + 1));
        }

        model.put("rows", rows);
        model.put("count", rows.size());

        return ThermalReceiptGenerator.renderSupplierReport(model);
    }

    public static byte[] SupplierReportA4Pro(List<SupplierEntity> suppliers) {
        List<SupplierEntity> safeSuppliers = suppliers == null ? List.of() : suppliers;
        Map<String, Object> model = baseModel("SUPPLIER REPORT");
        model.put("subtitle", "Supplier directory");
        List<Map<String, Object>> rows = new ArrayList<>();

        for (SupplierEntity supplier : safeSuppliers) {
            rows.add(supplierRow(supplier, rows.size() + 1));
        }

        model.put("rows", rows);
        model.put("count", rows.size());

        return HtmlReportRenderer.render(SUPPLIER_REPORT_A4, model,
                HtmlReportRenderer.PageSpec.a4());
    }

    // ================================================================
    // Due payment reports
    // ================================================================

    public static byte[] DuePaymentReportThermal(List<CustomerEntity> customers) {
        List<CustomerEntity> safeCustomers = customers == null ? List.of() : customers;
        Map<String, Object> model = baseModel("DUE PAYMENT REPORT");
        model.put("subtitle", "Outstanding customer balances");
        List<Map<String, Object>> rows = new ArrayList<>();
        double totalDue = 0;

        for (CustomerEntity customer : safeCustomers) {
            totalDue += customer.getDueAmount();
            rows.add(customerRow(customer, rows.size() + 1, false));
        }

        model.put("rows", rows);
        model.put("count", rows.size());
        model.put("totalDue", plainMoney(totalDue));

        return ThermalReceiptGenerator.renderDueReport(model);
    }

    public static byte[] DuePaymentReportA4Pro(List<CustomerEntity> customers) {
        List<CustomerEntity> safeCustomers = customers == null ? List.of() : customers;
        Map<String, Object> model = baseModel("DUE PAYMENT REPORT");
        model.put("subtitle", "Outstanding customer balances");
        List<Map<String, Object>> rows = new ArrayList<>();
        double totalDue = 0;

        for (CustomerEntity customer : safeCustomers) {
            totalDue += customer.getDueAmount();
            rows.add(customerRow(customer, rows.size() + 1, true));
        }

        model.put("rows", rows);
        model.put("count", rows.size());
        model.put("totalDue", money(totalDue));

        return HtmlReportRenderer.render(DUE_REPORT_A4, model,
                HtmlReportRenderer.PageSpec.a4());
    }

    // ================================================================
    // Product / inventory reports
    // ================================================================

    public static byte[] ProductReportThermal(List<ProductEntity> products) {
        List<ProductEntity> safeProducts = products == null ? List.of() : products;
        Map<String, Object> model = baseModel("INVENTORY REPORT");
        model.put("subtitle", "Stock levels and selling prices");
        List<Map<String, Object>> rows = new ArrayList<>();
        int lowStock = 0;

        for (ProductEntity product : safeProducts) {
            if (product.getQty() < 10) {
                lowStock++;
            }
            rows.add(productRow(product, rows.size() + 1, false));
        }

        model.put("rows", rows);
        model.put("count", rows.size());
        model.put("lowStock", lowStock);

        return ThermalReceiptGenerator.renderProductReport(model);
    }

    public static byte[] ProductReportA4Pro(List<ProductEntity> products) {
        List<ProductEntity> safeProducts = products == null ? List.of() : products;
        Map<String, Object> model = baseModel("INVENTORY REPORT");
        model.put("subtitle", "Stock levels and selling prices");
        List<Map<String, Object>> rows = new ArrayList<>();
        int lowStock = 0;

        for (ProductEntity product : safeProducts) {
            if (product.getQty() < 10) {
                lowStock++;
            }
            rows.add(productRow(product, rows.size() + 1, true));
        }

        model.put("rows", rows);
        model.put("count", rows.size());
        model.put("lowStock", lowStock);

        return HtmlReportRenderer.render(PRODUCT_REPORT_A4, model,
                HtmlReportRenderer.PageSpec.a4());
    }

    // ================================================================
    // Order invoice - 80mm thermal
    // ================================================================

    public static byte[] generateOrderBill(OrderEntity order, List<OrderProductDetailsEntity> details) {
        return generateOrderBill(order, details, 0.0);
    }

    public static byte[] generateOrderBill(OrderEntity order, List<OrderProductDetailsEntity> details,
                                           double duePaidAmount) {
        Map<String, Object> model = orderModel(order, details, duePaidAmount);

        return ThermalReceiptGenerator.renderOrderBill(model);
    }

    // ================================================================
    // Order invoice - A4 POS bill
    // ================================================================

    public static byte[] generateOrderBillA4(OrderEntity order, List<OrderProductDetailsEntity> details) {
        Map<String, Object> model = orderModel(order, details, 0.0);

        return HtmlReportRenderer.render(INVOICE_A4_BILL, model,
                HtmlReportRenderer.PageSpec.a4());
    }

    // ================================================================
    // Order invoice - A4 professional layout
    // ================================================================

    public static byte[] generateOrderBillA4Pro(OrderEntity order, List<OrderProductDetailsEntity> details) {
        return generateOrderBillA4Pro(order, details, 0.0);
    }

    public static byte[] generateOrderBillA4Pro(OrderEntity order, List<OrderProductDetailsEntity> details,
                                                double duePaidAmount) {
        Map<String, Object> model = orderModel(order, details, duePaidAmount);

        return HtmlReportRenderer.render(INVOICE_A4, model,
                HtmlReportRenderer.PageSpec.a4());
    }

    // ================================================================
    // Quotation - 80mm thermal
    // ================================================================

    public static byte[] generateQuotationBill80mm(
            String quotationId, LocalDate date, String customerName, String customerContact,
            List<CartTM> items, double discountPercent, double taxPercent) {

        Map<String, Object> model = quotationModel(quotationId, date, customerName, customerContact,
                items, discountPercent, taxPercent);

        return ThermalReceiptGenerator.renderQuotation(model);
    }

    // ================================================================
    // Quotation - A4 professional
    // ================================================================

    public static byte[] generateQuotationBillA4Pro(
            String quotationId, LocalDate date, String customerName, String customerContact,
            List<CartTM> items, double discountPercent, double taxPercent) {

        Map<String, Object> model = quotationModel(quotationId, date, customerName, customerContact,
                items, discountPercent, taxPercent);

        return HtmlReportRenderer.render(QUOTATION_A4, model,
                HtmlReportRenderer.PageSpec.a4());
    }

    // ================================================================
    // Sales summary - 80mm thermal
    // ================================================================

    public static byte[] SalesSummaryReportThermal(LocalDate date, List<ProductSalesTM> sales,
                                                   double totalAmount, double totalQty) {
        Map<String, Object> model = salesSummaryModel(date, sales, totalAmount, totalQty, false);

        return ThermalReceiptGenerator.renderSalesSummary(model);
    }

    // ================================================================
    // Sales summary - A4 professional
    // ================================================================

    public static byte[] SalesSummaryReportA4Pro(LocalDate date, List<ProductSalesTM> sales,
                                                 double totalAmount, double totalQty) {
        Map<String, Object> model = salesSummaryModel(date, sales, totalAmount, totalQty, true);

        return HtmlReportRenderer.render(SALES_SUMMARY_A4, model,
                HtmlReportRenderer.PageSpec.a4());
    }

    // ================================================================
    // Model builders
    // ================================================================

    private static Map<String, Object> baseModel(String reportTitle) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("companyName", COMPANY_NAME);
        model.put("companyTagline", COMPANY_TAGLINE);
        model.put("companyAddress", COMPANY_ADDRESS);
        model.put("companyPhone", COMPANY_PHONE);
        model.put("footerCompany", DEVELOPED_BY);
        model.put("reportTitle", reportTitle);
        model.put("generatedAt", LocalDateTime.now().format(DATE_FORMAT));
        return model;
    }

    private static Map<String, Object> customerRow(CustomerEntity customer, int index, boolean a4) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", text(customer.getCustId()));
        row.put("name", text(customer.getName()));
        row.put("contact", text(customer.getContact()));
        row.put("due", a4 ? money(customer.getDueAmount()) : plainMoney(customer.getDueAmount()));
        row.put("rowClass", index % 2 == 0 ? "alt" : "");
        return row;
    }

    private static Map<String, Object> supplierRow(SupplierEntity supplier, int index) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", text(supplier.getSupplierId()));
        row.put("name", text(supplier.getName()));
        row.put("contact", text(supplier.getContact()));
        row.put("rowClass", index % 2 == 0 ? "alt" : "");
        return row;
    }

    private static Map<String, Object> productRow(ProductEntity product, int index, boolean a4) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("itemCode", text(product.getItemcode()));
        row.put("name", text(product.getName()));
        row.put("price", a4 ? money(product.getPrice()) : plainMoney(product.getPrice()));
        row.put("qty", formatQty(product.getQty()));
        row.put("rowClass", index % 2 == 0 ? "alt" : "");
        return row;
    }

    private static Map<String, Object> orderModel(OrderEntity order,
                                                  List<OrderProductDetailsEntity> details,
                                                  double duePaidAmount) {
        Map<String, Object> model = baseModel("INVOICE");
        model.put("subtitle", "Sales invoice " + (order == null ? "-" : text(order.getOrderId())));
        List<Map<String, Object>> rows = new ArrayList<>();
        double subTotal = 0;

        if (details != null) {
            int index = 0;
            for (OrderProductDetailsEntity detail : details) {
                index++;
                double quantity = detail.getQuantity();
                double rate = detail.getUnitPrice();
                double amount = quantity * rate;
                subTotal += amount;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("index", index);
                row.put("name", detail.getProduct() != null
                        ? text(detail.getProduct().getName()) : "-");
                row.put("qty", formatQty(quantity));
                row.put("rate", plainMoney(rate));
                row.put("amount", plainMoney(amount));
                row.put("rowClass", index % 2 == 0 ? "alt" : "");
                rows.add(row);
            }
        }

        LocalDate date = order == null ? null : order.getDate();
        CustomerEntity customer = order == null ? null : order.getCustomer();
        String contact = customer == null ? null : customer.getContact();

        double discountPercent = order == null ? 0 : order.getDiscount();
        double discount = subTotal * discountPercent / 100.0;
        double taxRate = order == null ? 0 : order.getTaxRate();
        double tax = (subTotal - discount) * taxRate / 100.0;
        double total = subTotal - discount + tax;

        double amountPaid = order == null ? 0 : order.getAmountPaid();
        double balance = order == null ? 0 : order.getBalance();
        String paymentMethod = order == null ? null : order.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.isBlank()) {
            paymentMethod = "CASH";
        }

        String balanceSuffix = balance < 0 ? " (Due)" : (balance > 0 ? " (Change)" : "");
        String balanceClass = balance < 0 ? "negative" : (balance > 0 ? "positive" : "");
        String balanceLabel = balance < 0 ? "DUE" : (balance > 0 ? "CHANGE" : "PAID");

        LocalDateTime orderSavedAt = order == null ? null : order.getCreatedAt();
        if (orderSavedAt == null && date != null) {
            orderSavedAt = date.atStartOfDay();
        }

        model.put("orderId", order == null ? "-" : text(order.getOrderId()));
        model.put("orderDate", date == null ? "-" : date.format(BILL_DATE_FORMAT));
        model.put("orderDateTime", orderSavedAt == null ? "-" : orderSavedAt.format(BILL_DATETIME_FORMAT));
        model.put("customerName", customer == null ? "-" : text(customer.getName()));
        model.put("customerContact", text(contact));
        model.put("hasContact", hasText(contact));
        model.put("rows", rows);

        model.put("subTotal", plainMoney(subTotal));
        model.put("discount", plainMoney(discount));
        model.put("tax", plainMoney(tax));
        model.put("total", plainMoney(total));
        model.put("subTotalA4", money(subTotal));
        model.put("discountA4", money(discount));
        model.put("taxA4", money(tax));
        model.put("totalA4", money(total));

        model.put("discountPercent", String.format(Locale.US, "%.0f", discountPercent));
        model.put("taxRate", String.format(Locale.US, "%.1f", taxRate));
        model.put("paymentMethod", paymentMethod);
        model.put("amountPaid", plainMoney(amountPaid));
        model.put("amountPaidA4", money(amountPaid));
        model.put("hasDuePaid", duePaidAmount > 0.00001);
        model.put("duePaid", plainMoney(duePaidAmount));
        model.put("duePaidA4", money(duePaidAmount));
        model.put("balanceText", plainMoney(Math.abs(balance)));
        model.put("balanceTextA4", money(Math.abs(balance)));
        model.put("balanceSuffix", balanceSuffix);
        model.put("balanceClass", balanceClass);
        model.put("balanceLabel", balanceLabel);

        return model;
    }

    private static Map<String, Object> quotationModel(String quotationId, LocalDate date,
                                                      String customerName, String customerContact,
                                                      List<CartTM> items, double discountPercent,
                                                      double taxPercent) {
        Map<String, Object> model = baseModel("QUOTATION");
        model.put("subtitle", "Quotation " + text(quotationId));
        List<Map<String, Object>> rows = new ArrayList<>();
        double subTotal = 0;

        if (items != null) {
            int index = 0;
            for (CartTM item : items) {
                index++;
                double quantity = item.getQty();
                double rate = item.getUnitPrice();
                double amount = quantity * rate;
                subTotal += amount;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("index", index);
                row.put("description", text(item.getDescription()));
                row.put("qty", formatQty(quantity));
                row.put("rate", plainMoney(rate));
                row.put("amount", plainMoney(amount));
                row.put("rowClass", index % 2 == 0 ? "alt" : "");
                rows.add(row);
            }
        }

        double discount = subTotal * discountPercent / 100.0;
        double totalAfterDiscount = subTotal - discount;
        double tax = totalAfterDiscount * taxPercent / 100.0;
        double total = totalAfterDiscount + tax;

        LocalDateTime quotationSavedAt = date == null
                ? LocalDateTime.now()
                : date.atTime(LocalTime.now().withNano(0));

        model.put("quotationId", text(quotationId));
        model.put("quotationDate", date == null ? "-" : date.format(BILL_DATE_FORMAT));
        model.put("quotationDateTime", quotationSavedAt.format(BILL_DATETIME_FORMAT));
        model.put("customerName", text(customerName));
        model.put("customerContact", text(customerContact));
        model.put("hasContact", hasText(customerContact));
        model.put("rows", rows);

        model.put("subTotal", plainMoney(subTotal));
        model.put("discount", plainMoney(discount));
        model.put("tax", plainMoney(tax));
        model.put("total", plainMoney(total));
        model.put("subTotalA4", money(subTotal));
        model.put("discountA4", money(discount));
        model.put("taxA4", money(tax));
        model.put("totalA4", money(total));

        model.put("discountPercent", String.format(Locale.US, "%.0f", discountPercent));
        model.put("taxRate", String.format(Locale.US, "%.1f", taxPercent));

        return model;
    }

    private static Map<String, Object> salesSummaryModel(LocalDate date, List<ProductSalesTM> sales,
                                                         double totalAmount, double totalQty,
                                                         boolean a4) {
        Map<String, Object> model = baseModel("SALES SUMMARY REPORT");
        model.put("subtitle", "Date: " + (date == null ? "-" : date.toString()));
        List<Map<String, Object>> rows = new ArrayList<>();
        double calculatedTotal = 0;

        if (sales != null) {
            int index = 0;
            for (ProductSalesTM item : sales) {
                index++;
                calculatedTotal += item.getTotal();

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("code", text(item.getItemCode()));
                row.put("name", text(item.getName()));
                row.put("qty", formatQty(item.getQty()));
                row.put("total", a4 ? money(item.getTotal()) : plainMoney(item.getTotal()));
                row.put("rowClass", index % 2 == 0 ? "alt" : "");
                rows.add(row);
            }
        }

        double grandTotal = totalAmount != 0 ? totalAmount : calculatedTotal;

        model.put("date", date == null ? "-" : date.toString());
        model.put("rows", rows);
        model.put("soldQty", formatQty(totalQty));
        model.put("totalAmount", a4 ? money(grandTotal) : plainMoney(grandTotal));

        return model;
    }

    // ================================================================
    // Formatting helpers
    // ================================================================

    private static String money(double value) {
        return String.format(Locale.US, "%,.2f", value);
    }

    private static String plainMoney(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static String formatQty(double qty) {
        if (Math.abs(qty - Math.rint(qty)) < 0.000001) {
            return String.valueOf((long) Math.rint(qty));
        }
        return String.format(Locale.US, "%.2f", qty);
    }

    private static String text(String value) {
        return hasText(value) ? value : "-";
    }

    private static String truncate(String value, int maxLength) {
        String safe = text(value);
        if (safe.length() <= maxLength) {
            return safe;
        }
        return safe.substring(0, maxLength).trim() + "...";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
