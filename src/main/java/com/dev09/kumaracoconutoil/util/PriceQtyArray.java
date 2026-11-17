package com.dev09.kumaracoconutoil.util;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helpers for the product price + quantity 2D array that is stored as JSON text
 * in {@code product.price_qty}. Every row is {@code {price, qty}}.
 */
public final class PriceQtyArray {

    public static final int PRICE = 0;
    public static final int QTY = 1;

    private static final String NUMBER = "[+-]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?";
    private static final Pattern ROW = Pattern.compile(
            "\\[\\s*(" + NUMBER + ")\\s*,\\s*(" + NUMBER + ")\\s*\\]");

    private PriceQtyArray() {
    }

    /** Keep only valid, non-negative rows. */
    public static double[][] normalize(double[][] rows) {
        if (rows == null || rows.length == 0) return new double[0][];
        ArrayList<double[]> valid = new ArrayList<>();
        for (double[] row : rows) {
            if (row == null || row.length < 2) continue;
            double price = row[PRICE];
            double qty = row[QTY];
            if (!Double.isFinite(price) || !Double.isFinite(qty) || price < 0 || qty < 0) continue;
            valid.add(new double[]{price, qty});
        }
        return valid.toArray(new double[0][]);
    }

    /** Serialize to {@code [[1000.0,5.0],[900.0,10.0]]}. */
    public static String toJson(double[][] rows) {
        double[][] clean = normalize(rows);
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < clean.length; i++) {
            if (i > 0) builder.append(',');
            builder.append('[')
                    .append(Double.toString(clean[i][PRICE])).append(',')
                    .append(Double.toString(clean[i][QTY]))
                    .append(']');
        }
        return builder.append(']').toString();
    }

    /** Read the JSON text back into rows. Malformed or empty values give an empty array. */
    public static double[][] fromJson(String json) {
        if (json == null || json.isBlank() || json.indexOf('[') < 0) return new double[0][];
        Matcher matcher = ROW.matcher(json);
        ArrayList<double[]> rows = new ArrayList<>();
        while (matcher.find()) {
            try {
                double price = Double.parseDouble(matcher.group(1));
                double qty = Double.parseDouble(matcher.group(2));
                if (Double.isFinite(price) && Double.isFinite(qty) && price >= 0 && qty >= 0) {
                    rows.add(new double[]{price, qty});
                }
            } catch (NumberFormatException ignored) {
                // Skip a malformed pair rather than failing the whole product.
            }
        }
        return rows.toArray(new double[0][]);
    }

    public static double totalQty(double[][] rows) {
        double total = 0.0;
        if (rows == null) return total;
        for (double[] row : rows) {
            if (row != null && row.length > QTY) total += Math.max(0.0, row[QTY]);
        }
        return total;
    }

    /** The first (primary) price, or 0 when there are no rows. */
    public static double primaryPrice(double[][] rows) {
        if (rows == null || rows.length == 0 || rows[0] == null || rows[0].length == 0) return 0.0;
        return Math.max(0.0, rows[0][PRICE]);
    }

    public static ArrayList<Double> prices(double[][] rows) {
        ArrayList<Double> prices = new ArrayList<>();
        if (rows == null) return prices;
        for (double[] row : rows) {
            if (row != null && row.length > PRICE) prices.add(row[PRICE]);
        }
        return prices;
    }

    /** Display form used by the product table, e.g. {@code [1000, 900]}. */
    public static String formatPrices(double[][] rows) {
        ArrayList<Double> prices = prices(rows);
        if (prices.isEmpty()) return "[]";
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < prices.size(); i++) {
            if (i > 0) builder.append(", ");
            double value = prices.get(i);
            if (value == Math.rint(value)) builder.append((long) value);
            else builder.append(String.format(Locale.US, "%.2f", value));
        }
        return builder.append(']').toString();
    }

    /** Return rows whose total quantity is {@code target}; the first row absorbs the difference. */
    public static double[][] withTotalQty(double[][] rows, double target) {
        double[][] clean = normalize(rows);
        double desired = Math.max(0.0, target);
        if (clean.length == 0) return new double[][]{{0.0, desired}};
        double otherRows = 0.0;
        for (int i = 1; i < clean.length; i++) otherRows += clean[i][QTY];
        clean[0][QTY] = Math.max(0.0, desired - otherRows);
        return clean;
    }

    /** Deduct stock from the first tier, then the next, never going below zero. */
    public static double[][] deduct(double[][] rows, double amount) {
        double[][] clean = normalize(rows);
        double remaining = Math.max(0.0, amount);
        for (double[] row : clean) {
            if (remaining <= 0.0) break;
            double take = Math.min(row[QTY], remaining);
            row[QTY] -= take;
            remaining -= take;
        }
        return clean;
    }

    public static double[][] append(double[][] rows, double price, double qty) {
        double[][] clean = normalize(rows);
        double[][] result = new double[clean.length + 1][];
        System.arraycopy(clean, 0, result, 0, clean.length);
        result[clean.length] = new double[]{Math.max(0.0, price), Math.max(0.0, qty)};
        return result;
    }
}
