package com.parkwise.web;

import com.sun.net.httpserver.HttpExchange;
import com.parkwise.model.*;
import com.parkwise.service.ParkingService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Handles all /api/dashboard and /api/slots endpoints.
 */
public class DashboardHandler extends BaseHandler {

    private final ParkingService service;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public DashboardHandler(ParkingService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

        if ("OPTIONS".equals(method)) { handleOptions(ex); return; }

        if ("GET".equals(method)) {
            if (path.equals("/api/dashboard")) {
                handleDashboard(ex);
            } else if (path.equals("/api/slots")) {
                handleSlots(ex);
            } else {
                sendJson(ex, 404, error("Not found"));
            }
        } else {
            sendJson(ex, 405, error("Method not allowed"));
        }
    }

    private void handleDashboard(HttpExchange ex) throws IOException {
        // Stats
        int totalSlots   = service.getTotalSlots();
        int available    = service.getAvailableSlots();
        int occupied     = service.getOccupiedSlots();
        int parked       = service.getParkedVehicleCount();
        double todayRev  = service.getTodayRevenue();
        double totalRev  = service.getTotalRevenue();
        int bikeAvail    = service.getAvailableBikeSlots();
        int bikeTotal    = service.getTotalBikeSlots();
        int carAvail     = service.getAvailableCarSlots();
        int carTotal     = service.getTotalCarSlots();
        int todayEntries = service.getTodayEntries();
        int todayExits   = service.getTodayExits();

        // Currently parked vehicles
        List<ParkingRecord> parkedList = service.getParkedVehicles();
        List<String> parkedJson = new ArrayList<>();
        for (ParkingRecord r : parkedList) {
            double fee = service.calculateFee(r);
            parkedJson.add(Json.obj(
                "ticketId", r.getTicketId(),
                "vehicleNumber", r.getVehicleNumber(),
                "ownerName", r.getOwnerName(),
                "phone", r.getPhone(),
                "vehicleType", r.getVehicleType(),
                "slotNumber", r.getSlotNumber(),
                "entryTime", r.getEntryTime().format(DTF),
                "estimatedFee", fee
            ));
        }

        String json = Json.obj(
            "totalSlots", totalSlots,
            "available", available,
            "occupied", occupied,
            "parkedNow", parked,
            "todayRevenue", todayRev,
            "totalRevenue", totalRev,
            "todayEntries", todayEntries,
            "todayExits", todayExits,
            "bikeAvail", bikeAvail,
            "bikeTotal", bikeTotal,
            "carAvail", carAvail,
            "carTotal", carTotal,
            "bikeRate", service.getBikeRate(),
            "carRate", service.getCarRate(),
            "parkedVehicles", Json.arr(parkedJson)
        );
        sendJson(ex, 200, json);
    }

    private void handleSlots(HttpExchange ex) throws IOException {
        Map<String, String> params = queryParams(ex);
        String type = params.getOrDefault("type", "All");

        List<ParkingSlot> slots = "All".equals(type) ? service.getAllSlots() : service.getSlotsByType(type);
        List<String> items = new ArrayList<>();
        for (ParkingSlot s : slots) {
            items.add(Json.obj(
                "slotId", s.getSlotId(),
                "slotNumber", s.getSlotNumber(),
                "slotType", s.getSlotType(),
                "status", s.getStatus(),
                "floor", s.getFloorNumber()
            ));
        }
        sendJson(ex, 200, Json.arr(items));
    }
}
