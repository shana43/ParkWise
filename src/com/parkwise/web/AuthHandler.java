package com.parkwise.web;

import com.sun.net.httpserver.HttpExchange;
import com.parkwise.service.ParkingService;

import java.io.IOException;
import java.util.Map;

/**
 * Handles POST /api/login and POST /api/logout
 */
public class AuthHandler extends BaseHandler {

    private final ParkingService service;

    public AuthHandler(ParkingService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

        if ("OPTIONS".equals(method)) { handleOptions(ex); return; }

        if ("POST".equals(method) && path.equals("/api/login")) {
            Map<String, String> body = Json.parseBody(readBody(ex));
            String username = body.getOrDefault("username", "");
            String password = body.getOrDefault("password", "");

            if (username.isEmpty() || password.isEmpty()) {
                sendJson(ex, 400, error("Username and password required."));
                return;
            }

            if (service.authenticateAdmin(username, password)) {
                String name = service.getAdminName(username);
                sendJson(ex, 200, Json.obj("success", true, "message", "Login successful",
                        "username", username, "fullName", name));
            } else {
                sendJson(ex, 401, error("Invalid username or password."));
            }
            return;
        }

        sendJson(ex, 404, error("Not found."));
    }
}
