package com.dev09.kumaracoconutoil.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * Renders the HTML templates stored under {@code /pdfGeneratorFiles} to PDF
 * using openhtmltopdf (Flying Saucer + PDFBox).
 *
 * <p>openhtmltopdf is a server side HTML/CSS renderer. It does not execute
 * JavaScript, so the templates are bound with Java data before rendering.
 * The optional browser preview helper lives in
 * {@code pdfGeneratorFiles/js/report-preview.js}.</p>
 */
final class HtmlReportRenderer {

    private static final String CSS_RESOURCE = "/pdfGeneratorFiles/css/report.css";
    private static final String LOGO_RESOURCE = "/asets/logo.png";
    private static volatile String cachedCss;
    private static volatile String cachedLogoDataUri;

    static {
        // openhtmltopdf logs every render at INFO by default; keep the POS
        // console quiet while still surfacing warnings and errors.
        XRLog.setLevel(XRLog.LOAD, Level.WARNING);
        XRLog.setLevel(XRLog.MATCH, Level.WARNING);
        XRLog.setLevel(XRLog.GENERAL, Level.WARNING);
        XRLog.setLevel(XRLog.INIT, Level.WARNING);
        XRLog.setLevel(XRLog.CONFIG, Level.WARNING);
        XRLog.setLevel(XRLog.RENDER, Level.WARNING);
        XRLog.setLevel(XRLog.LAYOUT, Level.WARNING);
        XRLog.setLevel(XRLog.CASCADE, Level.WARNING);
        XRLog.setLevel(XRLog.CSS_PARSE, Level.WARNING);
        XRLog.setLevel(XRLog.XML_ENTITIES, Level.WARNING);
    }

    private HtmlReportRenderer() {
    }

    static byte[] render(String templateName, Map<String, Object> model, PageSpec page) {
        Map<String, Object> data = new HashMap<>(model);
        data.put("styles", stylesheet() + System.lineSeparator() + page.css());
        data.put("bodyClass", page.bodyClass());
        data.put("logoDataUri", logoDataUri());

        String html = TemplateEngine.render(templateName, data);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate PDF from template: " + templateName, e);
        }
    }

    private static String stylesheet() {
        String css = cachedCss;
        if (css == null) {
            synchronized (HtmlReportRenderer.class) {
                css = cachedCss;
                if (css == null) {
                    try (InputStream in = HtmlReportRenderer.class.getResourceAsStream(CSS_RESOURCE)) {
                        if (in == null) {
                            throw new IllegalStateException("Report stylesheet not found: " + CSS_RESOURCE);
                        }
                        css = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        cachedCss = css;
                    } catch (IOException e) {
                        throw new IllegalStateException("Unable to read report stylesheet", e);
                    }
                }
            }
        }
        return css;
    }

    /**
     * Reads the application logo from the classpath and exposes it to templates
     * as a data URI. This keeps the image working when the app is packaged as a
     * JAR, where file paths are not available.
     */
    private static String logoDataUri() {
        String uri = cachedLogoDataUri;
        if (uri == null) {
            synchronized (HtmlReportRenderer.class) {
                uri = cachedLogoDataUri;
                if (uri == null) {
                    try (InputStream in = HtmlReportRenderer.class.getResourceAsStream(LOGO_RESOURCE)) {
                        if (in == null) {
                            throw new IllegalStateException("Logo not found on classpath: " + LOGO_RESOURCE);
                        }
                        String base64 = Base64.getEncoder().encodeToString(in.readAllBytes());
                        uri = "data:image/png;base64," + base64;
                        cachedLogoDataUri = uri;
                    } catch (IOException e) {
                        throw new IllegalStateException("Unable to read report logo", e);
                    }
                }
            }
        }
        return uri;
    }

    /**
     * Page definition for a report. A4 pages use A4 portrait margins;
     * thermal report templates use a fixed 80mm page whose height is
     * calculated from the number of rows in the report.
     */
    static final class PageSpec {
        private final String css;
        private final String bodyClass;

        private PageSpec(String css, String bodyClass) {
            this.css = css;
            this.bodyClass = bodyClass;
        }

        String css() {
            return css;
        }

        String bodyClass() {
            return bodyClass;
        }

        static PageSpec a4() {
            return new PageSpec("""
                    @page {
                        size: A4;
                        margin: 18mm 16mm;
                    }
                    """, "a4");
        }

        static PageSpec thermal(double heightMm) {
            String height = String.format(Locale.US, "%.1f", Math.max(120.0, heightMm));
            return new PageSpec("""
                    @page {
                        size: 80mm %smm;
                        margin: 3mm;
                    }
                    """.formatted(height), "thermal");
        }
    }
}