package com.parkwise.ui.panels;

import com.parkwise.service.ParkingService;
import com.parkwise.ui.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Reports panel showing revenue analytics and vehicle statistics
 * with date range filtering and visual summary cards.
 */
public class ReportsPanel extends JPanel {

    private final ParkingService service;
    private JTextField fromDateField, toDateField;
    private JPanel reportContent;
    private final DateTimeFormatter dateParser = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public ReportsPanel(ParkingService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 15));
        setBackground(UIUtils.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        buildUI();
    }

    private void buildUI() {
        add(UIUtils.createSectionHeader("Reports & Analytics", "Revenue and vehicle statistics"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setOpaque(false);

        // Filter bar
        JPanel filterCard = UIUtils.createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 5));
        filterCard.setPreferredSize(new Dimension(0, 60));

        filterCard.add(UIUtils.createLabel("📅 Date Range:", UIUtils.FONT_BODY_BOLD, UIUtils.TEXT_SECONDARY));

        fromDateField = UIUtils.createTextField("dd-MM-yyyy");
        fromDateField.setPreferredSize(new Dimension(130, 36));
        filterCard.add(fromDateField);

        filterCard.add(UIUtils.createLabel("to", UIUtils.FONT_BODY, UIUtils.TEXT_SECONDARY));

        toDateField = UIUtils.createTextField("dd-MM-yyyy");
        toDateField.setPreferredSize(new Dimension(130, 36));
        filterCard.add(toDateField);

        JButton generateBtn = UIUtils.createButton("📊 Generate", UIUtils.PRIMARY);
        generateBtn.setPreferredSize(new Dimension(130, 36));
        generateBtn.addActionListener(e -> generateReport());
        filterCard.add(generateBtn);

        JButton todayBtn = UIUtils.createButton("📅 Today", UIUtils.SUCCESS);
        todayBtn.setPreferredSize(new Dimension(100, 36));
        todayBtn.addActionListener(e -> {
            String today = LocalDate.now().format(dateParser);
            fromDateField.setText(today);
            fromDateField.setForeground(UIUtils.TEXT_PRIMARY);
            toDateField.setText(today);
            toDateField.setForeground(UIUtils.TEXT_PRIMARY);
            generateReport();
        });
        filterCard.add(todayBtn);

        JButton weekBtn = UIUtils.createButton("📅 This Week", UIUtils.INFO);
        weekBtn.setPreferredSize(new Dimension(120, 36));
        weekBtn.addActionListener(e -> {
            LocalDate now = LocalDate.now();
            LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
            fromDateField.setText(weekStart.format(dateParser));
            fromDateField.setForeground(UIUtils.TEXT_PRIMARY);
            toDateField.setText(now.format(dateParser));
            toDateField.setForeground(UIUtils.TEXT_PRIMARY);
            generateReport();
        });
        filterCard.add(weekBtn);

        JButton monthBtn = UIUtils.createButton("📅 This Month", UIUtils.SECONDARY);
        monthBtn.setPreferredSize(new Dimension(130, 36));
        monthBtn.addActionListener(e -> {
            LocalDate now = LocalDate.now();
            LocalDate monthStart = now.withDayOfMonth(1);
            fromDateField.setText(monthStart.format(dateParser));
            fromDateField.setForeground(UIUtils.TEXT_PRIMARY);
            toDateField.setText(now.format(dateParser));
            toDateField.setForeground(UIUtils.TEXT_PRIMARY);
            generateReport();
        });
        filterCard.add(monthBtn);

        JButton allTimeBtn = UIUtils.createButton("📅 All Time", UIUtils.ACCENT_ORANGE);
        allTimeBtn.setPreferredSize(new Dimension(110, 36));
        allTimeBtn.addActionListener(e -> generateAllTimeReport());
        filterCard.add(allTimeBtn);

        content.add(filterCard, BorderLayout.NORTH);

        // Report content
        reportContent = new JPanel(new BorderLayout());
        reportContent.setOpaque(false);
        content.add(reportContent, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        // Load all-time report by default
        generateAllTimeReport();
    }

    private void generateReport() {
        String fromStr = UIUtils.getFieldText(fromDateField, "dd-MM-yyyy");
        String toStr = UIUtils.getFieldText(toDateField, "dd-MM-yyyy");

        if (fromStr.isEmpty() || toStr.isEmpty()) {
            UIUtils.showError(this, "Please enter both From and To dates.");
            return;
        }

        try {
            LocalDateTime from = LocalDate.parse(fromStr, dateParser).atStartOfDay();
            LocalDateTime to = LocalDate.parse(toStr, dateParser).atTime(LocalTime.MAX);
            buildReport(from, to, fromStr + " to " + toStr);
        } catch (Exception e) {
            UIUtils.showError(this, "Invalid date format. Use dd-MM-yyyy");
        }
    }

    private void generateAllTimeReport() {
        LocalDateTime from = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.now();
        buildReport(from, to, "All Time");
    }

    private void buildReport(LocalDateTime from, LocalDateTime to, String periodLabel) {
        reportContent.removeAll();

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Period label
        JLabel periodTitle = UIUtils.createLabel("📊  Report Period: " + periodLabel, UIUtils.FONT_HEADING, UIUtils.TEXT_PRIMARY);
        periodTitle.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(periodTitle);
        panel.add(Box.createVerticalStrut(20));

        // Revenue stats
        double totalRevenue = service.getRevenueByDateRange(from, to);
        double bikeRevenue = service.getRevenueByType("Bike", from, to);
        double carRevenue = service.getRevenueByType("Car", from, to);
        int bikeCount = service.getVehicleCountByType("Bike", from, to);
        int carCount = service.getVehicleCountByType("Car", from, to);
        int totalVehicles = bikeCount + carCount;

        // Revenue cards row
        JPanel revenueCards = new JPanel(new GridLayout(1, 3, 15, 0));
        revenueCards.setOpaque(false);
        revenueCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        revenueCards.setAlignmentX(LEFT_ALIGNMENT);

        revenueCards.add(UIUtils.createStatCard("Total Revenue", "₹" + String.format("%.0f", totalRevenue), "💰", UIUtils.SUCCESS));
        revenueCards.add(UIUtils.createStatCard("Bike Revenue", "₹" + String.format("%.0f", bikeRevenue), "🏍️", UIUtils.ACCENT_CYAN));
        revenueCards.add(UIUtils.createStatCard("Car Revenue", "₹" + String.format("%.0f", carRevenue), "🚗", UIUtils.ACCENT_ORANGE));

        panel.add(revenueCards);
        panel.add(Box.createVerticalStrut(20));

        // Vehicle count cards
        JPanel vehicleCards = new JPanel(new GridLayout(1, 3, 15, 0));
        vehicleCards.setOpaque(false);
        vehicleCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        vehicleCards.setAlignmentX(LEFT_ALIGNMENT);

        vehicleCards.add(UIUtils.createStatCard("Total Vehicles", String.valueOf(totalVehicles), "🚗", UIUtils.PRIMARY));
        vehicleCards.add(UIUtils.createStatCard("Bikes Parked", String.valueOf(bikeCount), "🏍️", UIUtils.INFO));
        vehicleCards.add(UIUtils.createStatCard("Cars Parked", String.valueOf(carCount), "🚗", UIUtils.WARNING));

        panel.add(vehicleCards);
        panel.add(Box.createVerticalStrut(25));

        // Detail and Chart row
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomRow.setOpaque(false);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        bottomRow.setAlignmentX(LEFT_ALIGNMENT);

        // Detailed breakdown card
        JPanel detailCard = UIUtils.createCard();
        detailCard.setLayout(new BoxLayout(detailCard, BoxLayout.Y_AXIS));
        detailCard.setAlignmentX(LEFT_ALIGNMENT);
        detailCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        JLabel detailTitle = UIUtils.createLabel("📋  Detailed Breakdown", UIUtils.FONT_HEADING, UIUtils.TEXT_PRIMARY);
        detailTitle.setAlignmentX(LEFT_ALIGNMENT);
        detailCard.add(detailTitle);
        detailCard.add(Box.createVerticalStrut(20));

        // Rate info
        addDetailRow(detailCard, "Bike Rate", "₹" + String.format("%.0f", service.getBikeRate()) + "/hour");
        addDetailRow(detailCard, "Car Rate", "₹" + String.format("%.0f", service.getCarRate()) + "/hour");

        detailCard.add(Box.createVerticalStrut(5));
        detailCard.add(UIUtils.createSeparator());
        detailCard.add(Box.createVerticalStrut(10));

        addDetailRow(detailCard, "Total Vehicles", String.valueOf(totalVehicles));
        addDetailRow(detailCard, "Bikes", bikeCount + " (" + (totalVehicles > 0 ? String.format("%.1f", (bikeCount * 100.0 / totalVehicles)) : "0") + "%)");
        addDetailRow(detailCard, "Cars", carCount + " (" + (totalVehicles > 0 ? String.format("%.1f", (carCount * 100.0 / totalVehicles)) : "0") + "%)");

        detailCard.add(Box.createVerticalStrut(5));
        detailCard.add(UIUtils.createSeparator());
        detailCard.add(Box.createVerticalStrut(10));

        addDetailRow(detailCard, "Total Revenue", "₹" + String.format("%.2f", totalRevenue));
        addDetailRow(detailCard, "Bike Revenue", "₹" + String.format("%.2f", bikeRevenue) + " (" + (totalRevenue > 0 ? String.format("%.1f", (bikeRevenue * 100.0 / totalRevenue)) : "0") + "%)");
        addDetailRow(detailCard, "Car Revenue", "₹" + String.format("%.2f", carRevenue) + " (" + (totalRevenue > 0 ? String.format("%.1f", (carRevenue * 100.0 / totalRevenue)) : "0") + "%)");

        if (totalVehicles > 0) {
            detailCard.add(Box.createVerticalStrut(5));
            detailCard.add(UIUtils.createSeparator());
            detailCard.add(Box.createVerticalStrut(10));

            double avgRevenue = totalRevenue / totalVehicles;
            addDetailRow(detailCard, "Avg Revenue/Vehicle", "₹" + String.format("%.2f", avgRevenue));
        }

        // Parking slot stats
        detailCard.add(Box.createVerticalStrut(5));
        detailCard.add(UIUtils.createSeparator());
        detailCard.add(Box.createVerticalStrut(10));

        addDetailRow(detailCard, "Current Occupancy", service.getOccupiedSlots() + "/" + service.getTotalSlots() + " slots");
        addDetailRow(detailCard, "Available Now", service.getAvailableSlots() + " slots");

        bottomRow.add(detailCard);

        // Chart cards
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        chartsPanel.setOpaque(false);
        chartsPanel.add(createBarChart("💰 Revenue Comparison", "Bikes", bikeRevenue, UIUtils.ACCENT_CYAN, "Cars", carRevenue, UIUtils.ACCENT_ORANGE));
        chartsPanel.add(createBarChart("🚗 Vehicle Count Comparison", "Bikes", bikeCount, UIUtils.INFO, "Cars", carCount, UIUtils.WARNING));
        bottomRow.add(chartsPanel);

        panel.add(bottomRow);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        reportContent.add(scrollPane, BorderLayout.CENTER);
        reportContent.revalidate();
        reportContent.repaint();
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setAlignmentX(LEFT_ALIGNMENT);

        row.add(UIUtils.createLabel(label, UIUtils.FONT_BODY, UIUtils.TEXT_SECONDARY), BorderLayout.WEST);
        JLabel val = UIUtils.createLabel(value, UIUtils.FONT_BODY_BOLD, UIUtils.TEXT_PRIMARY);
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(val, BorderLayout.EAST);

        panel.add(row);
        panel.add(Box.createVerticalStrut(4));
    }

    private JPanel createBarChart(String title, String label1, double val1, Color c1, String label2, double val2, Color c2) {
        JPanel chartPanel = UIUtils.createCard();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel titleLabel = UIUtils.createLabel(title, UIUtils.FONT_HEADING, UIUtils.TEXT_PRIMARY);
        chartPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel bars = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight() - 40;
                double max = Math.max(val1, val2);
                if (max == 0) max = 1;

                int bar1H = (int) ((val1 / max) * h);
                int bar2H = (int) ((val2 / max) * h);

                int barW = 50;
                int gap = 40;
                int startX = (w - (barW * 2 + gap)) / 2;
                if (startX < 0) startX = 10;
                
                // Draw grid lines
                g2.setColor(UIUtils.BORDER);
                g2.drawLine(20, h, w-20, h);

                // Draw Bar 1
                g2.setColor(c1);
                g2.fill(new RoundRectangle2D.Double(startX, h - bar1H, barW, bar1H, 6, 6));
                
                // Draw Bar 2
                g2.setColor(c2);
                g2.fill(new RoundRectangle2D.Double(startX + barW + gap, h - bar2H, barW, bar2H, 6, 6));

                g2.setColor(UIUtils.TEXT_PRIMARY);
                g2.setFont(UIUtils.FONT_SMALL);
                FontMetrics fm = g2.getFontMetrics();

                String l1 = label1 + " (" + (int)val1 + ")";
                String l2 = label2 + " (" + (int)val2 + ")";

                g2.drawString(l1, startX + (barW - fm.stringWidth(l1))/2, h + 20);
                g2.drawString(l2, startX + barW + gap + (barW - fm.stringWidth(l2))/2, h + 20);

                g2.dispose();
            }
        };
        bars.setOpaque(false);
        bars.setPreferredSize(new Dimension(200, 150));
        chartPanel.add(bars, BorderLayout.CENTER);
        return chartPanel;
    }
}
