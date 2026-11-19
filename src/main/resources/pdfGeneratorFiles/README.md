# Report templates

All PDF reports are generated from the HTML templates in this folder. The Java
code in `com.dev09.kumaracoconutoil.util.PdfGenerator` prepares the report data
and `HtmlReportRenderer` renders the HTML with
[openhtmltopdf](https://github.com/danfickle/openhtmltopdf) (Flying Saucer +
PDFBox).

PDF generation is fully server-side. **openhtmltopdf does not execute
JavaScript**, so every template is data-bound in Java before rendering.

## Layout

| Path | Purpose |
| --- | --- |
| `css/report.css` | Shared A4 and 80 mm stylesheet, embedded into every PDF. |
| `partials/header-report-a4.html` | Two-column company/title header for A4 reports. |
| `partials/company-a4.html` | Company masthead for A4 invoices and quotations. |
| `partials/company-thermal-report.html` | Centered company header for 80 mm reports. |
| `partials/company-thermal.html` | Company header for 80 mm POS bills. |
| `partials/footer-receipt.html` | Receipt footer with signature line. |
| `*-report-a4.html` | A4 report layouts using the green report theme. |
| `*-report-thermal.html` | 80 mm report layouts using the black/white receipt theme. |
| `invoice-a4.html` | Formal A4 invoice layout. |
| `invoice-a4-bill.html` | A4 POS sales bill used by `generateOrderBillA4`. |
| `invoice-thermal.html` | 80 mm POS cash memo. |
| `preview.html` + `js/report-preview.js` | Optional browser preview (never used by the app). |

## Template syntax

The templates use a small Mustache-like syntax implemented by
`TemplateEngine`:

```html
{{key}}                 <!-- HTML-escaped value -->
{{{key}}}               <!-- raw value, used for CSS and the logo data URI -->
{{#each rows}}...{{/each}}
{{#if hasContact}}...{{else}}...{{/if}}
{{> partials/company-a4}}
```

`{{#each}}` pushes each list item onto the context stack, so inside the loop
you can use `{{name}}`, `{{qty}}`, etc. Keys are resolved from the innermost
context outwards.

## Page sizes

`HtmlReportRenderer.PageSpec` supplies the `@page` rule for each report:

- A4 reports and bills use `size: A4` with `18mm 16mm` margins.
- 80 mm reports use `size: 80mm <height>mm`; the height is calculated from the
  number of rows so receipts do not spill onto a second page.

A4 reports use the two-column report mockup: logo and company details on the
left, report title and subtitle on the right, a green rule, a green table header
with white text, striped rows, and a light-green summary box. 80 mm reports use
the receipt mockup: centered company header, dashed dividers, compact
black/white tables, a bordered total, and a centered footer. Thermal PDFs
intentionally omit the logo to save receipt space. The POS invoice and quotation
use the same layouts: A4 uses the green report header and summary, while 80 mm
uses the black/white receipt layout. The palette lives in `css/report.css`: green
`#5fb947`, yellow `#f1b41e`, white `#ffffff`, and black `#000000`.

Invoice and quotation PDFs print the date with time. Invoices use the saved
`created_at` timestamp; quotations stamp the generation time because quotations
are not stored in the database. Both A4 and 80 mm invoices include the small
notice `** This Bill is Valid for 3 Days Only.`, and both quotation formats
include `**Condition applied`.

The logo is read from `/asets/logo.png` on the classpath and injected into every
template as the `logoDataUri` model value. It is embedded as a PNG data URI so
it works from the packaged JAR. Only the two A4 header partials render it; the
thermal partials intentionally omit it. The browser preview uses the same path
through `js/report-preview.js`.

## Editing templates

openhtmltopdf parses templates as XML, so they must stay well-formed:

- Close every tag and use `<br />`, not `<br>`.
- Escape raw `&` as `&amp;` and raw `<` as `&lt;` in static text.
- Keep XHTML-style attribute quoting (`class="value"`).
- Avoid unescaped ampersands in CSS comments.

Model values are escaped by `{{key}}`; only use `{{{key}}}` for trusted HTML
such as the stylesheet block.

## Browser preview

`preview.html` is a developer convenience. It loads the same templates, applies
`report.css`, and substitutes sample data using `js/report-preview.js`. Because
the browser uses `fetch`, serve the folder over HTTP rather than opening the
file directly:

```bash
cd src/main/resources/pdfGeneratorFiles
python -m http.server 8080
# then open http://localhost:8080/preview.html
```

Any static server works (IntelliJ, VS Code Live Server, `npx serve`, etc.).
