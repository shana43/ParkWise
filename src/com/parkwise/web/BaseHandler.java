package com.parkwise.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Base handler with helper methods for reading/writing HTTP requests and responses.
 */
public abstract class BaseHandler implements HttpHandler {

    protected String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    protected void sendFile(HttpExchange ex, byte[] bytes, String contentType) throws IOException {
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    protected void handleOptions(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ex.sendResponseHeaders(204, -1);
    }

    protected String ok(String message) {
        return Json.obj("success", true, "message", message);
    }

    protected String error(String message) {
        return Json.obj("success", false, "message", message);
    }

    /** Extract path segment: /api/records/42 → segment at index 2 = "42" */
    protected String pathSegment(HttpExchange ex, int index) {
        String[] parts = ex.getRequestURI().getPath().split("/");
        return (index < parts.length) ? parts[index] : "";
    }

    protected Map<String, String> queryParams(HttpExchange ex) {
        java.util.LinkedHashMap<String, String> map = new java.util.LinkedHashMap<>();
        String query = ex.getRequestURI().getRawQuery();
        if (query == null || query.isBlank()) return map;
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                try {
                    map.put(java.net.URLDecoder.decode(pair.substring(0, eq), "UTF-8"),
                            java.net.URLDecoder.decode(pair.substring(eq + 1), "UTF-8"));
                } catch (Exception ignored) {}
            }
        }
        return map;
    }
}
