package com.dev09.kumaracoconutoil.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very small HTML template engine used by {@link HtmlReportRenderer}.
 *
 * <p>Supported syntax:</p>
 * <ul>
 *   <li>{@code {{key}}} - HTML escaped value</li>
 *   <li>{@code {{{key}}}} - raw value (used for the shared CSS block)</li>
 *   <li>{@code {{#each rows}} ... {{/each}}} - loop over a list</li>
 *   <li>{@code {{#if key}} ... {{else}} ... {{/if}}} - conditional block</li>
 *   <li>{@code {{> partials/name}}} - include another template file</li>
 * </ul>
 *
 * <p>The syntax intentionally mirrors Mustache so the templates can also be
 * previewed in a browser with {@code pdfGeneratorFiles/js/report-preview.js}.</p>
 */
final class TemplateEngine {

    private static final String BASE_PATH = "/pdfGeneratorFiles/";
    private static final Map<String, List<Node>> CACHE = new ConcurrentHashMap<>();

    private TemplateEngine() {
    }

    static String render(String templateName, Map<String, Object> model) {
        List<Node> nodes = CACHE.computeIfAbsent(templateName, name -> {
            String path = name.startsWith("/") ? name : BASE_PATH + name;
            return parse(readResource(path));
        });

        StringBuilder out = new StringBuilder();
        Deque<Object> context = new ArrayDeque<>();
        context.push(model);
        for (Node node : nodes) {
            node.render(out, context);
        }
        return out.toString();
    }

    private static String readResource(String path) {
        try (InputStream in = TemplateEngine.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Report template not found on classpath: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read report template: " + path, e);
        }
    }

    // ------------------------------------------------------------------
    // Parsing
    // ------------------------------------------------------------------

    private static List<Node> parse(String template) {
        ParseResult result = parseNodes(template, 0, Collections.emptySet());
        if (result.stopTag != null) {
            throw new IllegalArgumentException("Unexpected closing tag {{" + result.stopTag + "}}");
        }
        return result.nodes;
    }

    private static ParseResult parseNodes(String text, int position, Set<String> stopTags) {
        List<Node> nodes = new ArrayList<>();

        while (true) {
            int start = text.indexOf("{{", position);
            if (start < 0) {
                if (position < text.length()) {
                    nodes.add(new TextNode(text.substring(position)));
                }
                return new ParseResult(nodes, null, text.length());
            }

            if (start > position) {
                nodes.add(new TextNode(text.substring(position, start)));
            }

            boolean raw = text.startsWith("{{{", start);
            int openLength = raw ? 3 : 2;
            String closeToken = raw ? "}}}" : "}}";
            int end = text.indexOf(closeToken, start + openLength);
            if (end < 0) {
                nodes.add(new TextNode(text.substring(start)));
                return new ParseResult(nodes, null, text.length());
            }

            String tag = text.substring(start + openLength, end).trim();
            position = end + closeToken.length();

            if (stopTags.contains(tag)) {
                return new ParseResult(nodes, tag, position);
            }
            if (tag.isEmpty()) {
                continue;
            }

            if (tag.startsWith("#each ")) {
                String expression = tag.substring(6).trim();
                ParseResult body = parseNodes(text, position, Set.of("else", "/each"));
                ParseResult alternate = null;
                position = body.position;
                if ("else".equals(body.stopTag)) {
                    alternate = parseNodes(text, position, Set.of("/each"));
                    position = alternate.position;
                } else if (!"/each".equals(body.stopTag)) {
                    throw new IllegalArgumentException("Unclosed {{#each " + expression + "}}");
                }
                nodes.add(new EachNode(expression,
                        body.nodes,
                        alternate == null ? Collections.emptyList() : alternate.nodes));
            } else if (tag.startsWith("#if ")) {
                String expression = tag.substring(4).trim();
                ParseResult body = parseNodes(text, position, Set.of("else", "/if"));
                ParseResult alternate = null;
                position = body.position;
                if ("else".equals(body.stopTag)) {
                    alternate = parseNodes(text, position, Set.of("/if"));
                    position = alternate.position;
                } else if (!"/if".equals(body.stopTag)) {
                    throw new IllegalArgumentException("Unclosed {{#if " + expression + "}}");
                }
                nodes.add(new IfNode(expression,
                        body.nodes,
                        alternate == null ? Collections.emptyList() : alternate.nodes));
            } else if (tag.startsWith("> ")) {
                nodes.add(new PartialNode(tag.substring(2).trim()));
            } else if (tag.startsWith("/") || tag.equals("else")) {
                throw new IllegalArgumentException("Unexpected template tag: {{" + tag + "}}");
            } else {
                nodes.add(new VariableNode(tag, raw));
            }
        }
    }

    // ------------------------------------------------------------------
    // Rendering helpers
    // ------------------------------------------------------------------

    private static Object resolve(String expression, Deque<Object> context) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }
        if ("this".equals(expression) || ".".equals(expression)) {
            return context.peek();
        }

        String[] parts = expression.split("\\.");
        Object value = null;
        for (Object frame : context) {
            if (frame instanceof Map<?, ?> map && map.containsKey(parts[0])) {
                value = map.get(parts[0]);
                break;
            }
        }

        for (int i = 1; i < parts.length && value != null; i++) {
            if (value instanceof Map<?, ?> map) {
                value = map.get(parts[i]);
            } else {
                return null;
            }
        }
        return value;
    }

    private static boolean truthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() > 0;
        }
        if (value instanceof Collection<?>) {
            return !((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map<?, ?>) {
            return !((Map<?, ?>) value).isEmpty();
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0.0;
        }
        return true;
    }

    private static String escapeHtml(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#39;");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }

    // ------------------------------------------------------------------
    // AST nodes
    // ------------------------------------------------------------------

    private interface Node {
        void render(StringBuilder out, Deque<Object> context);
    }

    private record ParseResult(List<Node> nodes, String stopTag, int position) {
    }

    private record TextNode(String text) implements Node {
        @Override
        public void render(StringBuilder out, Deque<Object> context) {
            out.append(text);
        }
    }

    private record VariableNode(String expression, boolean raw) implements Node {
        @Override
        public void render(StringBuilder out, Deque<Object> context) {
            Object value = resolve(expression, context);
            if (value == null) {
                return;
            }
            String text = String.valueOf(value);
            out.append(raw ? text : escapeHtml(text));
        }
    }

    private record IfNode(String expression, List<Node> nodes, List<Node> alternate) implements Node {
        @Override
        public void render(StringBuilder out, Deque<Object> context) {
            List<Node> branch = truthy(resolve(expression, context)) ? nodes : alternate;
            for (Node node : branch) {
                node.render(out, context);
            }
        }
    }

    private record EachNode(String expression, List<Node> nodes, List<Node> alternate) implements Node {
        @Override
        public void render(StringBuilder out, Deque<Object> context) {
            Object value = resolve(expression, context);
            if (value instanceof Iterable<?> iterable) {
                boolean rendered = false;
                for (Object item : iterable) {
                    rendered = true;
                    context.push(item == null ? Collections.emptyMap() : item);
                    try {
                        for (Node node : nodes) {
                            node.render(out, context);
                        }
                    } finally {
                        context.pop();
                    }
                }
                if (!rendered) {
                    for (Node node : alternate) {
                        node.render(out, context);
                    }
                }
            } else if (value != null && value.getClass().isArray()) {
                int length = java.lang.reflect.Array.getLength(value);
                if (length == 0) {
                    for (Node node : alternate) {
                        node.render(out, context);
                    }
                } else {
                    for (int i = 0; i < length; i++) {
                        Object item = java.lang.reflect.Array.get(value, i);
                        context.push(item == null ? Collections.emptyMap() : item);
                        try {
                            for (Node node : nodes) {
                                node.render(out, context);
                            }
                        } finally {
                            context.pop();
                        }
                    }
                }
            } else {
                for (Node node : alternate) {
                    node.render(out, context);
                }
            }
        }
    }

    private record PartialNode(String name) implements Node {
        @Override
        public void render(StringBuilder out, Deque<Object> context) {
            String templateName = name.endsWith(".html") ? name : name + ".html";
            List<Node> nodes = CACHE.computeIfAbsent(templateName, key -> {
                String path = key.startsWith("/") ? key : BASE_PATH + key;
                return parse(readResource(path));
            });
            for (Node node : nodes) {
                node.render(out, context);
            }
        }
    }
}
