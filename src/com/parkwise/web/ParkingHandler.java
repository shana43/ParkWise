package com.parkwise.web;

import com.sun.net.httpserver.HttpExchange;
import com.parkwise.model.*;
import com.parkwise.service.ParkingService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Handles all /api/parking/* endpoints:
 *   POST   /api/parking/entry          - park a vehicle
 *   POST   /api/parking/exit           - exit a vehicle
 *   GET    /api/parking/find?q=...     - find active record by ticket/vehicle
 *   GET    /api/parking/history        - all records, with optional search & date filters
 *   GET    /api/parking/active         - currently parked vehicles
 */
public class ParkingHandler extends BaseHandler {

    private final ParkingService service;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public ParkingHandler(ParkingService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

        if ("OPTIONS".equals(method)) { handleOptions(ex); return; }

        try {
            if ("POST".equals(method) && path.equals("/api/parking/entry")) {
                handleEntry(ex);
            } else if ("POST".equals(method) && path.equals("/api/parking/exit")) {
                handleExit(ex);
            } else if ("GET".equals(method) && path.equals("/api/parking/find")) {
                handleFind(ex);
            } else if ("GET".equals(method) && path.equals("/api/parking/history")) {
                handleHistory(ex);
            } else if ("GET".equals(method) && path.equals("/api/parking/active")) {
                handleActive(ex);
            } else {
                sendJson(ex, 404, error("Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(ex, 500, error("Server error: " + e.getMessage()));
        }
    }

    private void handleEntry(HttpExchange ex) throws IOException {
        Map<String, String> body = Json.parseBody(readBody(ex));
        String vehicleNum  = body.getOrDefault("vehicleNumber", "").toUpperCase().trim();
        String ownerName   = body.getOrDefault("ownerName", "").trim();
        String phone       = body.getOrDefault("phone", "").trim();
        String vehicleType = body.getOrDefault("vehicleType", "Car").trim();

        if (vehicleNum.isEmpty() || ownerName.isEmpty() || phone.isEmpty()) {
            sendJson(ex, 400, error("Vehicle number, owner name and phone are required."));
            return;
        }
        if (!vehicleNum.matches("[A-Z0-9]{4,20}")) {
            sendJson(ex, 400, error("Invalid vehicle number. Use 4-20 alphanumeric characters (no spaces). Example: KA01AB1234"));
            return;
        }
        if (!phone.matches("\\d{10}")) {
            sendJson(ex, 400, error("Phone must be 10 digits."));
            return;
        }
        if (service.getActiveByVehicleNumber(vehicleNum) != null) {
            sendJson(ex, 409, error("Vehicle " + vehicleNum + " is already parked."));
            return;
        }

        ParkingRecord record = service.parkVehicle(vehicleNum, ownerName, phone, vehicleType);
        if (record == null) {
            sendJson(ex, 503, error("No available " + vehicleType + " slots right now."));
            return;
        }

        double rate = "Car".equals(vehicleType) ? service.getCarRate() : service.getBikeRate();
        sendJson(ex, 200, Json.obj(
            "success", true,
            "message", "Vehicle parked successfully!",
            "ticketId", record.getTicketId(),
            "vehicleNumber", record.getVehicleNumber(),
            "ownerName", record.getOwnerName(),
            "phone", record.getPhone(),
            "vehicleType", record.getVehicleType(),
            "slotNumber", record.getSlotNumber(),
            "entryTime", record.getEntryTime().format(DTF),
            "rate", rate
        ));
    }

    private void handleExit(HttpExchange ex) throws IOException {
        Map<String, String> body = Json.parseBody(readBody(ex));
        String identifier     = body.getOrDefault("identifier", "").toUpperCase().trim();
        String paymentMethod  = body.getOrDefault("paymentMethod", "Cash").trim();

        if (identifier.isEmpty()) {
            sendJson(ex, 400, error("Ticket ID or vehicle number required."));
            return;
        }

        ParkingRecord record = service.exitVehicle(identifier, paymentMethod);
        if (record == null) {
            sendJson(ex, 404, error("No active parking record found for: " + identifier));
            return;
        }

        sendJson(ex, 200, Json.obj(
            "success", true,
            "message", "Vehicle exited successfully.",
            "ticketId", record.getTicketId(),
            "vehicleNumber", record.getVehicleNumber(),
            "ownerName", record.getOwnerName(),
            "phone", record.getPhone(),
            "vehicleType", record.getVehicleType(),
            "slotNumber", record.getSlotNumber(),
            "entryTime", record.getEntryTime().format(DTF),
            "exitTime", record.getExitTime().format(DTF),
            "duration", record.getFormattedDuration(),
            "durationMinutes", record.getDurationMinutes(),
            "amount", record.getAmount(),
            "paymentMethod", paymentMethod
        ));
    }

    private void handleFind(HttpExchange ex) throws IOException {
        Map<String, String> params = queryParams(ex);
        String q = params.getOrDefault("q", "").toUpperCase().trim();

        if (q.isEmpty()) {
            sendJson(ex, 400, error("Query parameter 'q' is required."));
            return;
        }

        ParkingRecord record = service.findActiveRecord(q);
        if (record == null) {
            sendJson(ex, 404, error("No active record found for: " + q));
            return;
        }

        // Calculate current fee estimate
        double fee = service.calculateFee(record);
        long mins = java.time.temporal.ChronoUnit.MINUTES.between(record.getEntryTime(), LocalDateTime.now());
        if (mins < 1) mins = 1;

        sendJson(ex, 200, Json.obj(
            "ticketId", record.getTicketId(),
            "vehicleNumber", record.getVehicleNumber(),
            "ownerName", record.getOwnerName(),
            "phone", record.getPhone(),
            "vehicleType", record.getVehicleType(),
            "slotNumber", record.getSlotNumber(),
            "entryTime", record.getEntryTime().format(DTF),
            "currentMinutes", mins,
            "estimatedFee", fee
        ));
    }

    private void handleHistory(HttpExchange ex) throws IOException {
        Map<String, String> params = queryParams(ex);
        String keyword  = params.getOrDefault("q", "").trim();
        String fromDate = params.getOrDefault("from", "").trim();
        String toDate   = params.getOrDefault("to", "").trim();

        List<ParkingRecord> records;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            if (!fromDate.isEmpty() && !toDate.isEmpty() && !keyword.isEmpty()) {
                LocalDateTime from = java.time.LocalDate.parse(fromDate, df).atStartOfDay();
                LocalDateTime to   = java.time.LocalDate.parse(toDate, df).atTime(java.time.LocalTime.MAX);
                records = service.getRecordsByDateRangeAndKeyword(from, to, keyword);
            } else if (!fromDate.isEmpty() && !toDate.isEmpty()) {
                LocalDateTime from = java.time.LocalDate.parse(fromDate, df).atStartOfDay();
                LocalDateTime to   = java.time.LocalDate.parse(toDate, df).atTime(java.time.LocalTime.MAX);
                records = service.getRecordsByDateRange(from, to);
            } else if (!keyword.isEmpty()) {
                records = service.searchRecords(keyword);
            } else {
                records = service.getAllRecords();
            }
        } catch (Exception e) {
            sendJson(ex, 400, error("Invalid date format. Use yyyy-MM-dd"));
            return;
        }

        List<String> items = new ArrayList<>();
        DateTimeFormatter displayDTF = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        for (ParkingRecord r : records) {
            items.add(Json.obj(
                "id", r.getId(),
                "ticketId", r.getTicketId(),
                "vehicleNumber", r.getVehicleNumber(),
                "ownerName", r.getOwnerName(),
                "phone", r.getPhone(),
                "vehicleType", r.getVehicleType(),
                "slotNumber", r.getSlotNumber(),
                "entryTime", r.getEntryTime().format(displayDTF),
                "exitTime", r.getExitTime() != null ? r.getExitTime().format(displayDTF) : "",
                "duration", r.getFormattedDuration(),
                "amount", r.getAmount() != null ? r.getAmount() : 0.0,
                "paymentStatus", r.getPaymentStatus(),
                "status", r.getStatus()
            ));
        }
        sendJson(ex, 200, Json.arr(items));
    }

    private void handleActive(HttpExchange ex) throws IOException {
        List<ParkingRecord> parked = service.getParkedVehicles();
        List<String> items = new ArrayList<>();
        DateTimeFormatter displayDTF = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        for (ParkingRecord r : parked) {
            long mins = java.time.temporal.ChronoUnit.MINUTES.between(r.getEntryTime(), LocalDateTime.now());
            double fee = service.calculateFee(r);
            items.add(Json.obj(
                "ticketId", r.getTicketId(),
                "vehicleNumber", r.getVehicleNumber(),
                "ownerName", r.getOwnerName(),
                "phone", r.getPhone(),
                "vehicleType", r.getVehicleType(),
                "slotNumber", r.getSlotNumber(),
                "entryTime", r.getEntryTime().format(displayDTF),
                "currentMinutes", mins,
                "estimatedFee", fee
            ));
        }
        sendJson(ex, 200, Json.arr(items));
    }
}
