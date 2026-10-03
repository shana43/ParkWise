package com.parkwise.web;

import com.parkwise.service.ParkingService;
import com.parkwise.db.DatabaseConnection;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.io.File;
import java.util.concurrent.Executors;

/**
 * ParkWise Web Server — entry point for the web application.
 * Starts an embedded HTTP server on port 8080, registers all API routes,
 * and serves the frontend from the ./web directory.
 */
public class WebServer {

    private static final int PORT = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 8080;

    public static void main(String[] args) throws Exception {
        // Prevent server from dying on uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            System.err.println("[ERROR] Uncaught exception in " + thread.getName() + ": " + ex.getMessage());
            ex.printStackTrace();
        });

        System.out.println("=========================================");
        System.out.println("  ParkWise Web Server starting...");
        System.out.println("=========================================");

        // Initialize database
        System.out.println("[DB] Initializing database...");
        DatabaseConnection.initializeDatabase();

        if (!DatabaseConnection.testConnection()) {
            System.err.println("[ERROR] Cannot connect to database!");
            System.err.println("  1. Make sure MySQL is running.");
            System.err.println("  2. Edit db.properties to set your password.");
            System.err.println("  3. Run schema.sql in MySQL to create the database.");
            System.exit(1);
        }
        System.out.println("[DB] Connected successfully.");

        // Create service
        ParkingService service = new ParkingService();

        // Find web root
        String webRoot = findWebRoot();
        System.out.println("[Static] Serving frontend from: " + webRoot);

        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 50);
        server.setExecutor(Executors.newCachedThreadPool());

        // Register API routes
        server.createContext("/api/login",           new AuthHandler(service));
        server.createContext("/api/dashboard",       new DashboardHandler(service));
        server.createContext("/api/slots",           new DashboardHandler(service));
        server.createContext("/api/parking/entry",  new ParkingHandler(service));
        server.createContext("/api/parking/exit",   new ParkingHandler(service));
        server.createContext("/api/parking/find",   new ParkingHandler(service));
        server.createContext("/api/parking/history",new ParkingHandler(service));
        server.createContext("/api/parking/active", new ParkingHandler(service));
        server.createContext("/api/vehicles",       new VehicleHandler(service));
        server.createContext("/api/reports",        new ReportsHandler(service));

        // Serve frontend (catch-all last)
        server.createContext("/", new StaticHandler(webRoot));

        // Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Server] Shutting down...");
            server.stop(2);
            System.out.println("[Server] Stopped.");
        }));

        server.start();

        System.out.println();
        System.out.println("=========================================");
        System.out.println("  ✅ ParkWise is running!");
        System.out.println("  🌐 Open: http://localhost:" + PORT);
        System.out.println("  🔑 Login: admin / admin123");
        System.out.println("=========================================");
        System.out.println("  Press Ctrl+C to stop.");
        System.out.println();
    }

    private static String findWebRoot() {
        // Try common locations relative to working directory
        String[] candidates = {"web", "src/web", "../web"};
        for (String c : candidates) {
            File f = new File(c);
            if (f.exists() && f.isDirectory() && new File(f, "index.html").exists()) {
                return f.getAbsolutePath();
            }
        }
        // Return default; StaticHandler will report file-not-found if missing
        return new File("web").getAbsolutePath();
    }
}
