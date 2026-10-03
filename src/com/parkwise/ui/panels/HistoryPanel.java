package com.parkwise.ui.panels;

import com.parkwise.model.ParkingRecord;
import com.parkwise.service.ParkingService;
import com.parkwise.ui.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Parking History panel with search, date range filters, and detailed record view.
 */
public class HistoryPanel extends JPanel {

    private final ParkingService service;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextField searchField, fromDateField, toDateField;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final DateTimeFormatter dateParser = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public HistoryPanel(ParkingService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 15));
        setBackground(UIUtils.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        buildUI();
    }

    private void buildUI() {
        add(UIUtils.createSectionHeader("Parking History", "View all parking records with search and filters"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setOpaque(false);

        // Filter bar
        JPanel filterCard = UIUtils.createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 5));
        filterCard.setPreferredSize(new Dimension(0, 60));

        // Search
        filterCard.add(UIUtils.createLabel("🔍", UIUtils.FONT_BODY, UIUtils.TEXT_SECONDARY));
        searchField = UIUtils.createTextField("Search by Ticket/Vehicle/Owner");
        searchField.setPreferredSize(new Dimension(250, 36));
        filterCard.add(searchField);

        // Date from
        filterCard.add(UIUtils.createLabel("From:", UIUtils.FONT_BODY_BOLD, UIUtils.TEXT_SECONDARY));
        fromDateField = UIUtils.createTextField("dd-MM-yyyy");
        fromDateField.setPreferredSize(new Dimension(130, 36));
        filterCard.add(fromDateField);

        // Date to
        filterCard.add(UIUtils.createLabel("To:", UIUtils.FONT_BODY_BOLD, UIUtils.TEXT_SECONDARY));
        toDateField = UIUtils.createTextField("dd-MM-yyyy");
        toDateField.setPreferredSize(new Dimension(130, 36));
        filterCard.add(toDateField);

        // Search button
        JButton searchBtn = UIUtils.createButton("🔍 Search", UIUtils.PRIMARY);
        searchBtn.setPreferredSize(new Dimension(110, 36));
        searchBtn.addActionListener(e -> search());
        filterCard.add(searchBtn);

        // Reset button
        JButton resetBtn = UIUtils.createButton("🔄 Reset", UIUtils.BG_HOVER);
        resetBtn.setPreferredSize(new Dimension(100, 36));
        resetBtn.addActionListener(e -> resetFilters());
        filterCard.add(resetBtn);

        content.add(filterCard, BorderLayout.NORTH);

        // Table
        JPanel tableCard = UIUtils.createCard();
        tableCard.setLayout(new BorderLayout());

        String[] columns = {"Ticket ID", "Vehicle No.", "Owner", "Phone", "Type", "Slot", "Entry Time", "Exit Time", "Duration", "Amount (₹)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        historyTable = new JTable(tableModel);
        UIUtils.styleTable(historyTable);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(120);

        tableCard.add(UIUtils.createStyledScrollPane(historyTable), BorderLayout.CENTER);
        content.add(tableCard, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
        loadAllRecords();
    }

    private void loadAllRecords() {
        tableModel.setRowCount(0);
        List<ParkingRecord> records = service.getAllRecords();
        populateTable(records);
    }

    private void search() {
        String keyword = UIUtils.getFieldText(searchField, "Search by Ticket/Vehicle/Owner");
        String fromStr = UIUtils.getFieldText(fromDateField, "dd-MM-yyyy");
        String toStr = UIUtils.getFieldText(toDateField, "dd-MM-yyyy");

        boolean hasKeyword = !keyword.isEmpty();
        boolean hasDateRange = !fromStr.isEmpty() && !toStr.isEmpty();

        try {
            List<ParkingRecord> records;

            if (hasDateRange && hasKeyword) {
                LocalDateTime from = LocalDate.parse(fromStr, dateParser).atStartOfDay();
                LocalDateTime to = LocalDate.parse(toStr, dateParser).atTime(LocalTime.MAX);
                records = service.getRecordsByDateRangeAndKeyword(from, to, keyword);
            } else if (hasDateRange) {
                LocalDateTime from = LocalDate.parse(fromStr, dateParser).atStartOfDay();
                LocalDateTime to = LocalDate.parse(toStr, dateParser).atTime(LocalTime.MAX);
                records = service.getRecordsByDateRange(from, to);
            } else if (hasKeyword) {
                records = service.searchRecords(keyword);
            } else {
                records = service.getAllRecords();
            }

            tableModel.setRowCount(0);
            populateTable(records);

            if (records.isEmpty()) {
                UIUtils.showWarning(this, "No records found matching the criteria.");
            }
        } catch (Exception e) {
            UIUtils.showError(this, "Invalid date format. Use dd-MM-yyyy");
        }
    }

    private void populateTable(List<ParkingRecord> records) {
        for (ParkingRecord r : records) {
            String exitTime = r.getExitTime() != null ? r.getExitTime().format(dtf) : "—";
            String duration = r.getFormattedDuration();
            String amount = r.getAmount() != null ? String.format("%.2f", r.getAmount()) : "—";

            tableModel.addRow(new Object[]{
                    r.getTicketId(),
                    r.getVehicleNumber(),
                    r.getOwnerName(),
                    r.getPhone(),
                    r.getVehicleType(),
                    r.getSlotNumber(),
                    r.getEntryTime().format(dtf),
                    exitTime,
                    duration,
                    amount,
                    r.getStatus()
            });
        }
    }

    private void resetFilters() {
        searchField.setText("Search by Ticket/Vehicle/Owner");
        searchField.setForeground(UIUtils.TEXT_MUTED);
        fromDateField.setText("dd-MM-yyyy");
        fromDateField.setForeground(UIUtils.TEXT_MUTED);
        toDateField.setText("dd-MM-yyyy");
        toDateField.setForeground(UIUtils.TEXT_MUTED);
        loadAllRecords();
    }

    public void refreshData() {
        loadAllRecords();
    }
}
