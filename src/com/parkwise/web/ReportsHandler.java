package com.parkwise.web;

import com.sun.net.httpserver.HttpExchange;
import com.parkwise.service.ParkingService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Handles GET /api/reports — revenue analytics by date range and vehicle type.
 */
public class ReportsHandler extends BaseHandler {

    private final ParkingService service;

    public ReportsHandler(ParkingService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        if ("OPTIONS".equals(method)) { handleOptions(ex); return; }

        if (!"GET".equals(method)) {
            sendJson(ex, 405, error("Method not allowed"));
            return;
        }

        try {
            Map<String, String> params = queryParams(ex);
            String fromStr = params.getOrDefault("from", "").trim();
            String toStr   = params.getOrDefault("to", "").trim();

            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime from, to;

            if (!fromStr.isEmpty() && !toStr.isEmpty()) {
                from = LocalDate.parse(fromStr, df).atStartOfDay();
                to   = LocalDate.parse(toStr, df).atTime(LocalTime.MAX);
            } else {
                // Default: last 30 days
                to   = LocalDateTime.now();
                from = to.minusDays(30).toLocalDate().atStartOfDay();
            }

            double totalRevenue  = service.getRevenueByDateRange(from, to);
            int    bikeCount     = service.getVehicleCountByType("Bike", from, to);
            int    carCount      = service.getVehicleCountByType("Car", from, to);
            double bikeRevenue   = service.getRevenueByType("Bike", from, to);
            double carRevenue    = service.getRevenueByType("Car", from, to);
            double bikeRate      = service.getBikeRate();
            double carRate       = service.getCarRate();
            int    totalCount    = bikeCount + carCount;

            // Dashboard-like stats for the reports page
            int totalSlots   = service.getTotalSlots();
            int available    = service.getAvailableSlots();
            int occupied     = service.getOccupiedSlots();
            int todayEntries = service.getTodayEntries();
            int todayExits   = service.getTodayExits();
            double todayRev  = service.getTodayRevenue();
            double allTimeRev= service.getTotalRevenue();

            DateTimeFormatter display = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            sendJson(ex, 200, Json.obj(
                "totalRevenue", totalRevenue,
                "bikeCount", bikeCount,
                "carCount", carCount,
                "totalCount", totalCount,
                "bikeRevenue", bikeRevenue,
                "carRevenue", carRevenue,
                "bikeRate", bikeRate,
                "carRate", carRate,
                "from", from.toLocalDate().format(display),
                "to", to.toLocalDate().format(display),
                "totalSlots", totalSlots,
                "available", available,
                "occupied", occupied,
                "todayEntries", todayEntries,
                "todayExits", todayExits,
                "todayRevenue", todayRev,
                "allTimeRevenue", allTimeRev
            ));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(ex, 400, error("Invalid date format. Use yyyy-MM-dd"));
        }
    }
}
