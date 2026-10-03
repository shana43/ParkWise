package com.parkwise.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.file.*;

/**
 * Serves static frontend files from the /web directory.
 * Maps / → index.html, /static/... → file.
 */
public class StaticHandler extends BaseHandler {

    private final String webRoot;

    public StaticHandler(String webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }

        String uriPath = ex.getRequestURI().getPath();
        if (uriPath.equals("/") || uriPath.isEmpty()) uriPath = "/index.html";

        // Prevent path traversal
        if (uriPath.contains("..")) {
            ex.sendResponseHeaders(403, -1);
            return;
        }

        File file = new File(webRoot + uriPath);
        if (!file.exists() || file.isDirectory()) {
            // SPA fallback — always serve index.html
            file = new File(webRoot + "/index.html");
        }

        if (!file.exists()) {
            ex.sendResponseHeaders(404, -1);
            return;
        }

        byte[] bytes = Files.readAllBytes(file.toPath());
        String contentType = getContentType(file.getName());
        sendFile(ex, bytes, contentType);
    }

    private String getContentType(String name) {
        if (name.endsWith(".html")) return "text/html; charset=UTF-8";
        if (name.endsWith(".css"))  return "text/css; charset=UTF-8";
        if (name.endsWith(".js"))   return "application/javascript; charset=UTF-8";
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".svg"))  return "image/svg+xml";
        if (name.endsWith(".ico"))  return "image/x-icon";
        if (name.endsWith(".json")) return "application/json";
        if (name.endsWith(".woff2")) return "font/woff2";
        if (name.endsWith(".woff"))  return "font/woff";
        return "application/octet-stream";
    }
}
