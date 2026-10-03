package com.parkwise.ui.panels;

import com.parkwise.model.ParkingRecord;
import com.parkwise.service.ParkingService;
import com.parkwise.ui.UIUtils;
import com.parkwise.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;

/**
 * Vehicle Exit panel for processing vehicle checkout.
 * Handles lookup by ticket ID or vehicle number, fee calculation, 
 * payment processing, and printable receipt generation.
 */
public class VehicleExitPanel extends JPanel {

    private final ParkingService service;
    private final MainFrame mainFrame;
    private JTextField searchField;
    private JPanel infoPanel, receiptPanel;
    private ParkingRecord currentRecord;
    private JComboBox<String> paymentCombo;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public VehicleExitPanel(ParkingService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 20));
        setBackground(UIUtils.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        buildUI();
    }

    private void buildUI() {
        add(UIUtils.createSectionHeader("Vehicle Exit", "Process vehicle checkout and generate bill"), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 25, 0));
        content.setOpaque(false);

        // Left: search + vehicle info
        JPanel leftCard = UIUtils.createCard();
        leftCard.setLayout(new BoxLayout(leftCard, BoxLayout.Y_AXIS));

        // Search bar
        JLabel searchLabel = UIUtils.createLabel("🔍  Search Vehicle", UIUtils.FONT_HEADING, UIUtils.TEXT_PRIMARY);
        searchLabel.setAlignmentX(LEFT_ALIGNMENT);
        leftCard.add(searchLabel);
        leftCard.add(Box.createVerticalStrut(10));

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        searchBar.setAlignmentX(LEFT_ALIGNMENT);

        searchField = UIUtils.createTextField("Enter Ticket ID or Vehicle Number");
        JButton findBtn = UIUtils.createButton("🔍 Find", UIUtils.PRIMARY);
        findBtn.setPreferredSize(new Dimension(110, 42));
        findBtn.addActionListener(e -> findVehicle());

        // Enter key to search
        searchField.addActionListener(e -> findVehicle());

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(findBtn, BorderLayout.EAST);
        leftCard.add(searchBar);
        leftCard.add(Box.createVerticalStrut(20));

        // Vehicle info panel
        infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setAlignmentX(LEFT_ALIGNMENT);
        showEmptyInfo();

        leftCard.add(infoPanel);
        content.add(leftCard);

        // Right: receipt
        receiptPanel = UIUtils.createCard();
        receiptPanel.setLayout(new BorderLayout());
        showEmptyReceipt();

        content.add(receiptPanel);
        add(content, BorderLayout.CENTER);
    }

    private void showEmptyInfo() {
        infoPanel.removeAll();
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel icon = UIUtils.createLabel("🔍", new Font("Segoe UI Emoji", Font.PLAIN, 48), UIUtils.TEXT_MUTED);
        icon.setAlignmentX(CENTER_ALIGNMENT);
        JLabel msg = UIUtils.createLabel("Search a vehicle to view details", UIUtils.FONT_BODY, UIUtils.TEXT_MUTED);
        msg.setAlignmentX(CENTER_ALIGNMENT);

        center.add(Box.createVerticalStrut(40));
        center.add(icon);
        center.add(Box.createVerticalStrut(10));
        center.add(msg);

        infoPanel.add(center);
        infoPanel.revalidate();
        infoPanel.repaint();
    }

    private void showEmptyReceipt() {
        receiptPanel.removeAll();
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel icon = UIUtils.createLabel("🧾", new Font("Segoe UI Emoji", Font.PLAIN, 64), UIUtils.TEXT_MUTED);
        icon.setAlignmentX(CENTER_ALIGNMENT);
        JLabel msg = UIUtils.createLabel("Receipt will appear here after exit", UIUtils.FONT_BODY, UIUtils.TEXT_MUTED);
        msg.setAlignmentX(CENTER_ALIGNMENT);

        center.add(Box.createVerticalGlue());
        center.add(icon);
        center.add(Box.createVerticalStrut(10));
        center.add(msg);
        center.add(Box.createVerticalGlue());

        receiptPanel.add(center, BorderLayout.CENTER);
        receiptPanel.revalidate();
        receiptPanel.repaint();
    }

    private void findVehicle() {
        String identifier = UIUtils.getFieldText(searchField, "Enter Ticket ID or Vehicle Number").trim().toUpperCase();
        if (identifier.isEmpty()) {
            UIUtils.showError(this, "Please enter a Ticket ID or Vehicle Number.");
            return;
        }

        currentRecord = service.findActiveRecord(identifier);
        if (currentRecord == null) {
            UIUtils.showError(this, "No active parking record found for: " + identifier);
            showEmptyInfo();
            return;
        }

        showVehicleInfo(currentRecord);
    }

    private void showVehicleInfo(ParkingRecord record) {
        infoPanel.removeAll();

        JLabel title = UIUtils.createLabel("📋  Vehicle Details", UIUtils.FONT_HEADING, UIUtils.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        infoPanel.add(title);
        infoPanel.add(Box.createVerticalStrut(15));

        addInfoRow("Ticket ID", record.getTicketId());
        addInfoRow("Vehicle No.", record.getVehicleNumber());
        addInfoRow("Owner", record.getOwnerName());
        addInfoRow("Phone", record.getPhone());
        addInfoRow("Type", record.getVehicleType());
        addInfoRow("Slot", record.getSlotNumber());
        addInfoRow("Entry Time", record.getEntryTime().format(dtf));

        // Calculate current duration and fee
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(record.getEntryTime(), now);
        if (minutes < 1) minutes = 1;
        int hours = (int) (minutes / 60);
        int mins = (int) (minutes % 60);
        String durationStr = hours > 0 ? hours + "h " + mins + "m" : mins + "m";

        double fee = service.calculateFee(record);

        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(UIUtils.createSeparator());
        infoPanel.add(Box.createVerticalStrut(10));

        addInfoRow("Duration", durationStr);
        addInfoRowColored("Estimated Fee", "₹" + String.format("%.2f", fee), UIUtils.WARNING);

        infoPanel.add(Box.createVerticalStrut(15));

        // Payment method
        JLabel payLabel = UIUtils.createFormLabel("Payment Method");
        payLabel.setAlignmentX(LEFT_ALIGNMENT);
        infoPanel.add(payLabel);
        paymentCombo = UIUtils.createComboBox(new String[]{"Cash", "Card", "UPI"});
        paymentCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        paymentCombo.setAlignmentX(LEFT_ALIGNMENT);
        infoPanel.add(paymentCombo);
        infoPanel.add(Box.createVerticalStrut(20));

        // Exit button
        JButton exitBtn = UIUtils.createButton("🚪  Process Exit", UIUtils.SUCCESS);
        exitBtn.setPreferredSize(new Dimension(200, 46));
        exitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        exitBtn.setAlignmentX(LEFT_ALIGNMENT);
        exitBtn.addActionListener(e -> processExit());
        infoPanel.add(exitBtn);

        infoPanel.revalidate();
        infoPanel.repaint();
    }

    private void addInfoRow(String label, String value) {
        addInfoRowColored(label, value, UIUtils.TEXT_PRIMARY);
    }

    private void addInfoRowColored(String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(LEFT_ALIGNMENT);

        row.add(UIUtils.createLabel(label, UIUtils.FONT_BODY, UIUtils.TEXT_SECONDARY), BorderLayout.WEST);
        JLabel val = UIUtils.createLabel(value, UIUtils.FONT_BODY_BOLD, valueColor);
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(val, BorderLayout.EAST);

        infoPanel.add(row);
        infoPanel.add(Box.createVerticalStrut(5));
    }

    private void processExit() {
        if (currentRecord == null) return;

        String paymentMethod = (String) paymentCombo.getSelectedItem();
        boolean confirm = UIUtils.showConfirm(this,
                "Process exit for vehicle " + currentRecord.getVehicleNumber() + "?\n" +
                        "Payment method: " + paymentMethod);
        if (!confirm) return;

        ParkingRecord exitRecord = service.exitVehicle(currentRecord.getTicketId(), paymentMethod);
        if (exitRecord != null) {
            showReceipt(exitRecord, paymentMethod);
            UIUtils.showSuccess(this, "Vehicle exited successfully!\nAmount: ₹" + String.format("%.2f", exitRecord.getAmount()));
            showEmptyInfo();
            currentRecord = null;
            searchField.setText("Enter Ticket ID or Vehicle Number");
            searchField.setForeground(UIUtils.TEXT_MUTED);
            mainFrame.refreshDashboard();
        } else {
            UIUtils.showError(this, "Failed to process exit. Please try again.");
        }
    }

    private void showReceipt(ParkingRecord record, String paymentMethod) {
        receiptPanel.removeAll();
        receiptPanel.setLayout(new BorderLayout());

        JPanel receipt = new JPanel();
        receipt.setOpaque(false);
        receipt.setLayout(new BoxLayout(receipt, BoxLayout.Y_AXIS));

        // Receipt header
        JLabel header = UIUtils.createLabel("🧾  PARKING RECEIPT", UIUtils.FONT_SUBTITLE, UIUtils.PRIMARY);
        header.setAlignmentX(CENTER_ALIGNMENT);
        receipt.add(Box.createVerticalStrut(5));
        receipt.add(header);

        JLabel brand = UIUtils.createLabel("ParkWise Management System", UIUtils.FONT_SMALL, UIUtils.TEXT_MUTED);
        brand.setAlignmentX(CENTER_ALIGNMENT);
        receipt.add(Box.createVerticalStrut(3));
        receipt.add(brand);
        receipt.add(Box.createVerticalStrut(15));
        receipt.add(UIUtils.createSeparator());
        receipt.add(Box.createVerticalStrut(10));

        // Receipt body
        addReceiptRow(receipt, "Ticket ID", record.getTicketId());
        addReceiptRow(receipt, "Vehicle No.", record.getVehicleNumber());
        addReceiptRow(receipt, "Owner", record.getOwnerName());
        addReceiptRow(receipt, "Type", record.getVehicleType());
        addReceiptRow(receipt, "Slot", record.getSlotNumber());

        receipt.add(Box.createVerticalStrut(5));
        receipt.add(UIUtils.createSeparator());
        receipt.add(Box.createVerticalStrut(5));

        addReceiptRow(receipt, "Entry Time", record.getEntryTime().format(dtf));
        addReceiptRow(receipt, "Exit Time", record.getExitTime().format(dtf));
        addReceiptRow(receipt, "Duration", record.getFormattedDuration());
        addReceiptRow(receipt, "Payment", paymentMethod);

        receipt.add(Box.createVerticalStrut(5));
        receipt.add(UIUtils.createSeparator());
        receipt.add(Box.createVerticalStrut(10));

        // Total amount
        JLabel totalLabel = UIUtils.createLabel("TOTAL AMOUNT", UIUtils.FONT_BODY, UIUtils.TEXT_SECONDARY);
        totalLabel.setAlignmentX(CENTER_ALIGNMENT);
        receipt.add(totalLabel);

        JLabel totalAmount = UIUtils.createLabel("₹" + String.format("%.2f", record.getAmount()),
                new Font("Segoe UI", Font.BOLD, 36), UIUtils.SUCCESS);
        totalAmount.setAlignmentX(CENTER_ALIGNMENT);
        receipt.add(totalAmount);

        receipt.add(Box.createVerticalStrut(10));

        JLabel paidBadge = UIUtils.createBadge("✅  PAID", UIUtils.SUCCESS);
        paidBadge.setAlignmentX(CENTER_ALIGNMENT);
        receipt.add(paidBadge);

        receipt.add(Box.createVerticalStrut(15));

        // Print button
        JButton printBtn = UIUtils.createButton("🖨️  Print Receipt", UIUtils.PRIMARY);
        printBtn.setAlignmentX(CENTER_ALIGNMENT);
        printBtn.setMaximumSize(new Dimension(200, 44));
        printBtn.addActionListener(e -> printReceipt(record, paymentMethod));
        receipt.add(printBtn);

        receiptPanel.add(receipt, BorderLayout.CENTER);
        receiptPanel.revalidate();
        receiptPanel.repaint();
    }

    private void addReceiptRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        row.setAlignmentX(LEFT_ALIGNMENT);

        row.add(UIUtils.createLabel(label, UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY), BorderLayout.WEST);
        JLabel val = UIUtils.createLabel(value, UIUtils.FONT_BODY_BOLD, UIUtils.TEXT_PRIMARY);
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(val, BorderLayout.EAST);

        panel.add(row);
        panel.add(Box.createVerticalStrut(4));
    }

    private void printReceipt(ParkingRecord record, String paymentMethod) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("ParkWise Receipt - " + record.getTicketId());

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            int y = 30;
            int x = 30;
            int lineHeight = 22;

            g2.setFont(new Font("Monospaced", Font.BOLD, 18));
            g2.drawString("PARKWISE - PARKING RECEIPT", x, y); y += lineHeight + 5;

            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g2.drawString("================================", x, y); y += lineHeight;
            g2.drawString("Ticket ID   : " + record.getTicketId(), x, y); y += lineHeight;
            g2.drawString("Vehicle No. : " + record.getVehicleNumber(), x, y); y += lineHeight;
            g2.drawString("Owner       : " + record.getOwnerName(), x, y); y += lineHeight;
            g2.drawString("Phone       : " + record.getPhone(), x, y); y += lineHeight;
            g2.drawString("Type        : " + record.getVehicleType(), x, y); y += lineHeight;
            g2.drawString("Slot        : " + record.getSlotNumber(), x, y); y += lineHeight;
            g2.drawString("================================", x, y); y += lineHeight;
            g2.drawString("Entry Time  : " + record.getEntryTime().format(dtf), x, y); y += lineHeight;
            g2.drawString("Exit Time   : " + record.getExitTime().format(dtf), x, y); y += lineHeight;
            g2.drawString("Duration    : " + record.getFormattedDuration(), x, y); y += lineHeight;
            g2.drawString("Payment     : " + paymentMethod, x, y); y += lineHeight;
            g2.drawString("================================", x, y); y += lineHeight;

            g2.setFont(new Font("Monospaced", Font.BOLD, 16));
            g2.drawString("TOTAL: Rs. " + String.format("%.2f", record.getAmount()), x, y); y += lineHeight + 5;

            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2.drawString("Thank you for using ParkWise!", x, y);

            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
                UIUtils.showSuccess(this, "Receipt sent to printer!");
            } catch (PrinterException ex) {
                UIUtils.showError(this, "Print failed: " + ex.getMessage());
            }
        }
    }
}
