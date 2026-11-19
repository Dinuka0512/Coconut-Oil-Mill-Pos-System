/*
 * Dilshan Oil Mill - optional browser preview helper.
 *
 * This file is NOT used when generating PDFs. PDF generation happens on the
 * server with openhtmltopdf, which does not execute JavaScript. The helper
 * lets a developer open the same HTML templates in a browser to check the
 * layout quickly.
 *
 * Start any static file server from src/main/resources/pdfGeneratorFiles and
 * open preview.html, for example:
 *
 *     python -m http.server 8080
 *     http://localhost:8080/preview.html
 *
 * The templates use a small Mustache-like syntax:
 *   {{key}}                 HTML-escaped value
 *   {{{key}}}               raw value
 *   {{#each rows}}...{{/each}}
 *   {{#if key}}...{{else}}...{{/if}}
 *   {{> partials/name}}
 */
(function (global) {
    'use strict';

    var scriptUrl = document.currentScript ? document.currentScript.src : null;
    var baseUrl = scriptUrl ? new URL('../', scriptUrl) : new URL('./', window.location.href);

    var templateCache = new Map();
    var partialCache = new Map();
    var resolvedPartials = new Map();

    function loadText(relativePath) {
        return fetch(new URL(relativePath, baseUrl)).then(function (response) {
            if (!response.ok) {
                throw new Error('Unable to load ' + relativePath + ' (' + response.status + ')');
            }
            return response.text();
        });
    }

    function loadTemplate(name) {
        var relativePath = name.indexOf('/') === 0 ? name.substring(1) : name;
        if (!templateCache.has(relativePath)) {
            templateCache.set(relativePath, loadText(relativePath).then(parse));
        }
        return templateCache.get(relativePath);
    }

    function loadPartial(name) {
        var relativePath = name.endsWith('.html') ? name : name + '.html';
        if (!partialCache.has(relativePath)) {
            partialCache.set(relativePath, loadText(relativePath).then(parse));
        }
        return partialCache.get(relativePath);
    }

    function parse(text) {
        var result = parseNodes(text, 0, new Set());
        if (result.stopTag) {
            throw new Error('Unexpected closing tag {{' + result.stopTag + '}}');
        }
        return result.nodes;
    }

    function parseNodes(text, position, stopTags) {
        var nodes = [];
        while (true) {
            var start = text.indexOf('{{', position);
            if (start < 0) {
                if (position < text.length) {
                    nodes.push({ type: 'text', text: text.substring(position) });
                }
                return { nodes: nodes, stopTag: null, position: text.length };
            }
            if (start > position) {
                nodes.push({ type: 'text', text: text.substring(position, start) });
            }

            var raw = text.startsWith('{{{', start);
            var openLength = raw ? 3 : 2;
            var closeToken = raw ? '}}}' : '}}';
            var end = text.indexOf(closeToken, start + openLength);
            if (end < 0) {
                nodes.push({ type: 'text', text: text.substring(start) });
                return { nodes: nodes, stopTag: null, position: text.length };
            }

            var tag = text.substring(start + openLength, end).trim();
            position = end + closeToken.length;

            if (stopTags.has(tag)) {
                return { nodes: nodes, stopTag: tag, position: position };
            }
            if (!tag) {
                continue;
            }

            if (tag.startsWith('#each ')) {
                var eachExpression = tag.substring(6).trim();
                var body = parseNodes(text, position, new Set(['else', '/each']));
                var alternate = null;
                position = body.position;
                if (body.stopTag === 'else') {
                    alternate = parseNodes(text, position, new Set(['/each']));
                    position = alternate.position;
                } else if (body.stopTag !== '/each') {
                    throw new Error('Unclosed {{#each ' + eachExpression + '}}');
                }
                nodes.push({
                    type: 'each',
                    expression: eachExpression,
                    nodes: body.nodes,
                    alternate: alternate ? alternate.nodes : []
                });
            } else if (tag.startsWith('#if ')) {
                var ifExpression = tag.substring(4).trim();
                var ifBody = parseNodes(text, position, new Set(['else', '/if']));
                var ifAlternate = null;
                position = ifBody.position;
                if (ifBody.stopTag === 'else') {
                    ifAlternate = parseNodes(text, position, new Set(['/if']));
                    position = ifAlternate.position;
                } else if (ifBody.stopTag !== '/if') {
                    throw new Error('Unclosed {{#if ' + ifExpression + '}}');
                }
                nodes.push({
                    type: 'if',
                    expression: ifExpression,
                    nodes: ifBody.nodes,
                    alternate: ifAlternate ? ifAlternate.nodes : []
                });
            } else if (tag.startsWith('> ')) {
                nodes.push({ type: 'partial', name: tag.substring(2).trim() });
            } else if (tag.startsWith('/') || tag === 'else') {
                throw new Error('Unexpected template tag {{' + tag + '}}');
            } else {
                nodes.push({ type: 'variable', expression: tag, raw: raw });
            }
        }
    }

    function resolve(expression, context) {
        if (!expression) {
            return null;
        }
        if (expression === 'this' || expression === '.') {
            return context[context.length - 1];
        }

        var parts = expression.split('.');
        var value = null;
        for (var i = context.length - 1; i >= 0; i--) {
            var frame = context[i];
            if (frame && typeof frame === 'object' && Object.prototype.hasOwnProperty.call(frame, parts[0])) {
                value = frame[parts[0]];
                break;
            }
        }
        for (var p = 1; p < parts.length && value != null; p++) {
            value = typeof value === 'object' ? value[parts[p]] : null;
        }
        return value;
    }

    function truthy(value) {
        if (value == null) {
            return false;
        }
        if (typeof value === 'boolean') {
            return value;
        }
        if (typeof value === 'string') {
            return value.length > 0;
        }
        if (Array.isArray(value)) {
            return value.length > 0;
        }
        if (typeof value === 'number') {
            return value !== 0;
        }
        return true;
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function renderNodes(nodes, context) {
        var output = '';
        nodes.forEach(function (node) {
            if (node.type === 'text') {
                output += node.text;
            } else if (node.type === 'variable') {
                var value = resolve(node.expression, context);
                if (value != null) {
                    output += node.raw ? String(value) : escapeHtml(value);
                }
            } else if (node.type === 'if') {
                output += renderNodes(truthy(resolve(node.expression, context)) ? node.nodes : node.alternate, context);
            } else if (node.type === 'each') {
                var collection = resolve(node.expression, context);
                if (Array.isArray(collection) && collection.length > 0) {
                    collection.forEach(function (item) {
                        context.push(item == null ? {} : item);
                        try {
                            output += renderNodes(node.nodes, context);
                        } finally {
                            context.pop();
                        }
                    });
                } else {
                    output += renderNodes(node.alternate, context);
                }
            } else if (node.type === 'partial') {
                var partialName = node.name.endsWith('.html') ? node.name : node.name + '.html';
                var partial = resolvedPartials.get(partialName);
                if (!partial) {
                    throw new Error('Partial not loaded: ' + node.name);
                }
                output += renderNodes(partial, context);
            }
        });
        return output;
    }

    function renderResolved(nodes, model) {
        return renderNodes(nodes, [model]);
    }

    function pageClassFor(templateName) {
        return templateName.indexOf('-a4') >= 0 ? 'a4' : 'thermal';
    }

    function pageCssFor(bodyClass) {
        if (bodyClass === 'a4') {
            return '@page { size: A4; margin: 18mm 16mm; }';
        }
        return '@page { size: 80mm 160mm; margin: 3mm; }';
    }

    function collectPartials(nodes, collected) {
        nodes.forEach(function (node) {
            if (node.type === 'partial') {
                collected.add(node.name.endsWith('.html') ? node.name : node.name + '.html');
            } else if (node.type === 'each' || node.type === 'if') {
                collectPartials(node.nodes, collected);
                collectPartials(node.alternate, collected);
            }
        });
        return collected;
    }

    function preloadPartials(nodes) {
        var names = Array.from(collectPartials(nodes, new Set()));
        return Promise.all(names.map(function (name) {
            var relativePath = name.indexOf('/') === 0 ? name.substring(1) : name;
            if (resolvedPartials.has(relativePath)) {
                return Promise.resolve();
            }
            return loadPartial(relativePath).then(function (partialNodes) {
                resolvedPartials.set(relativePath, partialNodes);
                return preloadPartials(partialNodes);
            });
        }));
    }

    function render(templateName, model) {
        return loadTemplate(templateName).then(function (nodes) {
            return preloadPartials(nodes).then(function () {
                var bodyClass = pageClassFor(templateName);
                return loadText('css/report.css').then(function (css) {
                    var data = Object.assign({}, model, {
                        bodyClass: bodyClass,
                        styles: css + '\n' + pageCssFor(bodyClass) + '\n' + (model.styles || '')
                    });
                    return renderResolved(nodes, data);
                });
            });
        });
    }

    function sampleModel(overrides) {
        var rows = [
            { rowClass: 'alt', id: 'C001', name: 'Nimal Perera', contact: '0771234567', due: '1,250.50',
                itemCode: 'ITM-001', price: '450.00', qty: '20', index: 1, description: 'Coconut Oil 1L',
                rate: '450.00', amount: '900.00', code: 'ITM-001', total: '6,750.00' },
            { rowClass: '', id: 'C002', name: 'S. Fernando and Sons', contact: '0719876543', due: '0.00',
                itemCode: 'ITM-002', price: '2,100.00', qty: '4', index: 2, description: 'Coconut Oil 5L',
                rate: '2,100.00', amount: '2,100.00', code: 'ITM-002', total: '8,400.00' }
        ];
        return Object.assign({
            companyName: 'DILSHAN OIL MILL',
            companyTagline: 'Your Trusted Partner in Coconut Products',
            companyAddress: '309/a 5 Baddegoda Rd, Bandaragama 12530',
            companyPhone: 'Tel: 077-6518819 | 077-7307805',
            logoDataUri: '../asets/logo.png',
            footerCompany: 'Developed by Dev09Solutions.com',
            reportTitle: 'CUSTOMER REPORT',
            subtitle: 'Customer directory',
            generatedAt: new Date().toLocaleString(),
            rows: rows,
            count: rows.length,
            totalDue: '1,250.50',
            lowStock: 1,
            orderId: 'ORD-1001',
            orderDate: '19-11-2026',
            orderDateTime: '19-11-2026 10:30:00',
            quotationId: 'Q-1001',
            quotationDate: '19-11-2026',
            quotationDateTime: '19-11-2026 10:30:00',
            customerName: 'Nimal Perera',
            customerContact: '0771234567',
            hasContact: true,
            subTotal: '3000.00',
            discount: '60.00',
            tax: '14.70',
            total: '2954.70',
            subTotalA4: '3,000.00',
            discountA4: '60.00',
            taxA4: '14.70',
            totalA4: '2,954.70',
            discountPercent: '2',
            taxRate: '0.5',
            paymentMethod: 'CASH',
            amountPaid: '1000.00',
            amountPaidA4: '1,000.00',
            hasDuePaid: true,
            duePaid: '500.00',
            duePaidA4: '500.00',
            balanceText: '100.91',
            balanceTextA4: '100.91',
            balanceSuffix: ' (Change)',
            balanceClass: 'positive',
            balanceLabel: 'CHANGE',
            date: '2026-11-19',
            soldQty: '19',
            totalAmount: '15,150.00'
        }, overrides || {});
    }

    function renderToIframe(iframe, templateName, model) {
        return render(templateName, sampleModel(model)).then(function (html) {
            iframe.srcdoc = html;
            return html;
        });
    }

    global.ReportPreview = {
        render: render,
        renderToIframe: renderToIframe,
        sampleModel: sampleModel,
        templates: [
            'customer-report-a4.html',
            'customer-report-thermal.html',
            'supplier-report-a4.html',
            'supplier-report-thermal.html',
            'due-report-a4.html',
            'due-report-thermal.html',
            'product-report-a4.html',
            'product-report-thermal.html',
            'invoice-a4.html',
            'invoice-a4-bill.html',
            'invoice-thermal.html',
            'quotation-a4.html',
            'quotation-thermal.html',
            'sales-summary-a4.html',
            'sales-summary-thermal.html'
        ]
    };
})(window);
