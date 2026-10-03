package com.parkwise.ui.panels;

import com.parkwise.model.ParkingRecord;
import com.parkwise.service.ParkingService;
import com.parkwise.ui.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard panel showing parking lot statistics and currently parked vehicles.
 */
public class DashboardPanel extends JPanel {

    private final ParkingService service;
    private JPanel statsPanel;
    private JTable parkedTable;
    private DefaultTableModel tableModel;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public DashboardPanel(ParkingService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 20));
        setBackground(UIUtils.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        buildUI();
    }

    private void buildUI() {
        // Header
        add(UIUtils.createSectionHeader("Dashboard", "Real-time parking lot overview"), BorderLayout.NORTH);

        // Main content
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BorderLayout(0, 20));

        // Stats cards
        statsPanel = new JPanel(new GridLayout(1, 6, 15, 0));
        statsPanel.setOpaque(false);
        content.add(statsPanel, BorderLayout.NORTH);

        // Parked vehicles table
        JPanel tableCard = UIUtils.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JLabel tableTitle = UIUtils.createLabel("🚗  Currently Parked Vehicles", UIUtils.FONT_HEADING, UIUtils.TEXT_PRIMARY);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Ticket ID", "Vehicle No.", "Owner", "Phone", "Type", "Slot", "Entry Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        parkedTable = new JTable(tableModel);
        UIUtils.styleTable(parkedTable);
        tableCard.add(UIUtils.createStyledScrollPane(parkedTable), BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        // Update stats
        statsPanel.removeAll();

        int totalSlots = service.getTotalSlots();
        int available = service.getAvailableSlots();
        int occupied = service.getOccupiedSlots();
        int parkedCount = service.getParkedVehicleCount();
        double todayRev = service.getTodayRevenue();
        double totalRev = service.getTotalRevenue();

        statsPanel.add(UIUtils.createStatCard("Total Slots", String.valueOf(totalSlots), "🅿️", UIUtils.PRIMARY));
        statsPanel.add(UIUtils.createStatCard("Available", String.valueOf(available), "✅", UIUtils.SUCCESS));
        statsPanel.add(UIUtils.createStatCard("Occupied", String.valueOf(occupied), "🔴", UIUtils.DANGER));
        statsPanel.add(UIUtils.createStatCard("Parked Now", String.valueOf(parkedCount), "🚗", UIUtils.INFO));
        statsPanel.add(UIUtils.createStatCard("Today Revenue", "₹" + String.format("%.0f", todayRev), "💰", UIUtils.WARNING));
        statsPanel.add(UIUtils.createStatCard("Total Revenue", "₹" + String.format("%.0f", totalRev), "💎", UIUtils.SECONDARY));

        statsPanel.revalidate();
        statsPanel.repaint();

        // Update parked vehicles table
        tableModel.setRowCount(0);
        List<ParkingRecord> parked = service.getParkedVehicles();
        for (ParkingRecord r : parked) {
            tableModel.addRow(new Object[]{
                    r.getTicketId(),
                    r.getVehicleNumber(),
                    r.getOwnerName(),
                    r.getPhone(),
                    r.getVehicleType(),
                    r.getSlotNumber(),
                    r.getEntryTime().format(dtf)
            });
        }
    }
}
