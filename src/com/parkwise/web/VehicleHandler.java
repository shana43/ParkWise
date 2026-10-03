package com.parkwise.web;

import com.sun.net.httpserver.HttpExchange;
import com.parkwise.model.Vehicle;
import com.parkwise.service.ParkingService;

import java.io.IOException;
import java.util.*;

/**
 * Handles /api/vehicles CRUD:
 *   GET    /api/vehicles          - list all (optional ?q= search)
 *   POST   /api/vehicles          - add new vehicle
 *   PUT    /api/vehicles/{id}     - update vehicle
 *   DELETE /api/vehicles/{id}     - delete vehicle
 */
public class VehicleHandler extends BaseHandler {

    private final ParkingService service;

    public VehicleHandler(ParkingService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();

        if ("OPTIONS".equals(method)) { handleOptions(ex); return; }

        try {
            // Check if there's an ID: /api/vehicles/42
            String[] parts = path.split("/");
            boolean hasId = parts.length >= 4 && !parts[3].isEmpty();
            int id = hasId ? Integer.parseInt(parts[3]) : -1;

            switch (method) {
                case "GET":    handleGet(ex, queryParams(ex).getOrDefault("q", "").trim()); break;
                case "POST":   handleAdd(ex);         break;
                case "PUT":    handleUpdate(ex, id);  break;
                case "DELETE": handleDelete(ex, id);  break;
                default: sendJson(ex, 405, error("Method not allowed"));
            }
        } catch (NumberFormatException e) {
            sendJson(ex, 400, error("Invalid vehicle ID."));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(ex, 500, error("Server error: " + e.getMessage()));
        }
    }

    private void handleGet(HttpExchange ex, String keyword) throws IOException {
        List<Vehicle> list = keyword.isEmpty() ? service.getAllVehicles() : service.searchVehicles(keyword);
        List<String> items = new ArrayList<>();
        for (Vehicle v : list) {
            items.add(Json.obj(
                "id", v.getId(),
                "vehicleNumber", v.getVehicleNumber(),
                "ownerName", v.getOwnerName(),
                "phone", v.getPhone(),
                "vehicleType", v.getVehicleType()
            ));
        }
        sendJson(ex, 200, Json.arr(items));
    }

    private void handleAdd(HttpExchange ex) throws IOException {
        Map<String, String> body = Json.parseBody(readBody(ex));
        Vehicle v = buildVehicle(-1, body);
        if (v == null) { sendJson(ex, 400, error("Missing required fields.")); return; }

        if (!validate(ex, v)) return;
        if (service.addVehicle(v)) {
            sendJson(ex, 200, Json.obj("success", true, "message", "Vehicle added.", "id", v.getId()));
        } else {
            sendJson(ex, 500, error("Failed to add vehicle."));
        }
    }

    private void handleUpdate(HttpExchange ex, int id) throws IOException {
        if (id < 0) { sendJson(ex, 400, error("Vehicle ID required.")); return; }
        Map<String, String> body = Json.parseBody(readBody(ex));
        Vehicle v = buildVehicle(id, body);
        if (v == null) { sendJson(ex, 400, error("Missing required fields.")); return; }

        if (!validate(ex, v)) return;
        if (service.updateVehicle(v)) {
            sendJson(ex, 200, Json.obj("success", true, "message", "Vehicle updated."));
        } else {
            sendJson(ex, 500, error("Failed to update vehicle."));
        }
    }

    private void handleDelete(HttpExchange ex, int id) throws IOException {
        if (id < 0) { sendJson(ex, 400, error("Vehicle ID required.")); return; }
        if (service.deleteVehicle(id)) {
            sendJson(ex, 200, Json.obj("success", true, "message", "Vehicle deleted."));
        } else {
            sendJson(ex, 500, error("Failed to delete vehicle."));
        }
    }

    private Vehicle buildVehicle(int id, Map<String, String> body) {
        String num   = body.getOrDefault("vehicleNumber", "").toUpperCase().trim();
        String name  = body.getOrDefault("ownerName", "").trim();
        String phone = body.getOrDefault("phone", "").trim();
        String type  = body.getOrDefault("vehicleType", "Car").trim();
        if (num.isEmpty() || name.isEmpty() || phone.isEmpty()) return null;
        return id < 0 ? new Vehicle(num, name, phone, type) : new Vehicle(id, num, name, phone, type);
    }

    private boolean validate(HttpExchange ex, Vehicle v) throws IOException {
        if (!v.getVehicleNumber().matches("[A-Z0-9]{4,20}")) {
            sendJson(ex, 400, error("Invalid vehicle number. Use 4-20 alphanumeric characters. Example: KA01AB1234")); return false;
        }
        if (!v.getPhone().matches("\\d{10}")) {
            sendJson(ex, 400, error("Phone must be 10 digits.")); return false;
        }
        return true;
    }
}
