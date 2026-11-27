package com.dev09.kumaracoconutoil.util;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/** Runs document generation and printing off the JavaFX application thread. */
public final class PrintUtil {

    private PrintUtil() {
    }

    /** Executes a print job in the background and reports failures on the UI thread. */
    public static void runAsync(String errorMessage, Callable<Void> job) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                job.call();
                return null;
            }
        };
        task.setOnFailed(e -> {
            Throwable failure = task.getException();
            failure.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, errorMessage + ": " + failure.getMessage());
        });
        Thread thread = new Thread(task, "print-job");
        thread.setDaemon(true);
        thread.start();
    }

    /** Generates PDF bytes in the background, then opens the temporary file. */
    public static void openPdfAsync(String prefix, Callable<byte[]> generator) {
        runAsync("Error generating PDF", () -> {
            byte[] bytes = generator.call();
            if (bytes != null) {
                Path temp = Files.createTempFile(prefix, ".pdf");
                Files.write(temp, bytes);
                temp.toFile().deleteOnExit();
                Desktop.getDesktop().open(temp.toFile());
            }
            return null;
        });
    }

    /** Generates ESC/POS bytes in the background and sends them to the thermal printer. */
    public static void printThermalAsync(Callable<byte[]> generator) {
        runAsync("Error printing", () -> {
            ThermalPrinter.print(generator.call());
            return null;
        });
    }
}
