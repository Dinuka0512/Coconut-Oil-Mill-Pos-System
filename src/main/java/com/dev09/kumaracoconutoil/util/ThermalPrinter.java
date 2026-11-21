package com.dev09.kumaracoconutoil.util;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.output.PrinterOutputStream;

import javax.print.PrintService;

/**
 * Sends ESC/POS receipt bytes to an 80mm thermal printer.
 *
 * <p>The printer is selected with the {@code dilshan.thermal.printer} system
 * property or the {@code THERMAL_PRINTER} environment variable. When neither
 * is set, the system's default print service is used.</p>
 *
 * <p>If no printer is available or printing fails, the same receipt is opened as
 * an 80mm PDF preview in the default PDF viewer.</p>
 */
public final class ThermalPrinter {

    private static final String PRINTER_PROPERTY = "dilshan.thermal.printer";
    private static final String PRINTER_ENV = "THERMAL_PRINTER";

    private ThermalPrinter() {
    }

    public static void print(byte[] escpos) {
        if (escpos == null || escpos.length == 0) {
            throw new IllegalArgumentException("No ESC/POS receipt content to print.");
        }

        try {
            PrintService printer = findPrinter();
            try (PrinterOutputStream stream = new PrinterOutputStream(printer)) {
                EscPos escposPrinter = new EscPos(stream);
                escposPrinter.write(escpos, 0, escpos.length);
                escposPrinter.flush();
            }
        } catch (Exception printError) {
            System.err.println("Thermal printer unavailable (" + printError.getMessage()
                    + "). Opening an 80mm PDF preview instead.");
            try {
                ThermalPdfRenderer.open(escpos);
            } catch (Exception pdfError) {
                throw new IllegalStateException(
                        "Thermal printing failed and the 80mm PDF preview could not be opened: "
                                + pdfError.getMessage(), printError);
            }
        }
    }

    private static PrintService findPrinter() {
        String configured = System.getProperty(PRINTER_PROPERTY);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(PRINTER_ENV);
        }
        try {
            if (configured != null && !configured.isBlank()) {
                return PrinterOutputStream.getPrintServiceByName(configured.trim());
            }
            return PrinterOutputStream.getDefaultPrintService();
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "No thermal printer found. Set -D" + PRINTER_PROPERTY + "=<printer name> "
                            + "or the " + PRINTER_ENV + " environment variable.", e);
        }
    }
}
