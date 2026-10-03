package com.parkwise.ui.panels;

import com.parkwise.model.ParkingRecord;
import com.parkwise.service.ParkingService;
import com.parkwise.ui.UIUtils;
import com.parkwise.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Vehicle Entry panel for parking new vehicles.
 * Handles form input, validation, automatic slot allocation, and ticket generation.
 */
public class VehicleEntryPanel extends JPanel {

    private final ParkingService service;
    private final MainFrame mainFrame;
    private JTextField vehicleNumField, ownerField, phoneField;
    private JComboBox<String> typeCombo;
    private JPanel ticketPanel;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public VehicleEntryPanel(ParkingService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 20));
        setBackground(UIUtils.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        buildUI();
    }

    private void buildUI() {
        add(UIUtils.createSectionHeader("Vehicle Entry", "Park a new vehicle and generate ticket"), BorderLayout.NORTH);

        // Split: form on left, ticket preview on right
        JPanel content = new JPanel(new GridLayout(1, 2, 25, 0));
        content.setOpaque(false);

        // Form card
        JPanel formCard = UIUtils.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JLabel formTitle = UIUtils.createLabel("🚗  Vehicle Details", UIUtils.FONT_HEADING, UIUtils.TEXT_PRIMARY);
        formTitle.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(formTitle);
        formCard.add(Box.createVerticalStrut(20));

        // Vehicle Number
        formCard.add(createFormRow("Vehicle Number"));
        vehicleNumField = UIUtils.createTextField("e.g., KA01AB1234");
        vehicleNumField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        vehicleNumField.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(vehicleNumField);
        formCard.add(Box.createVerticalStrut(15));

        // Owner Name
        formCard.add(createFormRow("Owner Name"));
        ownerField = UIUtils.createTextField("e.g., Rahul Sharma");
        ownerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        ownerField.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(ownerField);
        formCard.add(Box.createVerticalStrut(15));

        // Phone
        formCard.add(createFormRow("Phone Number"));
        phoneField = UIUtils.createTextField("e.g., 9876543210");
        phoneField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        phoneField.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(phoneField);
        formCard.add(Box.createVerticalStrut(15));

        // Vehicle Type
        formCard.add(createFormRow("Vehicle Type"));
        typeCombo = UIUtils.createComboBox(new String[]{"Car", "Bike"});
        typeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        typeCombo.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(typeCombo);
        formCard.add(Box.createVerticalStrut(10));

        // Slot availability info
        JPanel slotInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        slotInfo.setOpaque(false);
        slotInfo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel bikeSlots = UIUtils.createBadge("🏍️ Bikes: " + service.getAvailableBikeSlots() + "/" + service.getTotalBikeSlots(), UIUtils.SUCCESS);
        JLabel carSlots = UIUtils.createBadge("🚗 Cars: " + service.getAvailableCarSlots() + "/" + service.getTotalCarSlots(), UIUtils.INFO);
        slotInfo.add(bikeSlots);
        slotInfo.add(carSlots);
        formCard.add(slotInfo);
        formCard.add(Box.createVerticalStrut(25));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton parkBtn = UIUtils.createButton("🅿️  Park Vehicle", UIUtils.PRIMARY);
        parkBtn.setPreferredSize(new Dimension(180, 44));
        parkBtn.addActionListener(e -> parkVehicle());

        JButton clearBtn = UIUtils.createButton("🔄  Clear", UIUtils.BG_HOVER);
        clearBtn.setPreferredSize(new Dimension(130, 44));
        clearBtn.addActionListener(e -> clearForm());

        btnPanel.add(parkBtn);
        btnPanel.add(clearBtn);
        formCard.add(btnPanel);

        content.add(formCard);

        // Ticket preview card
        ticketPanel = UIUtils.createCard();
        ticketPanel.setLayout(new BorderLayout());
        showEmptyTicket();

        content.add(ticketPanel);
        add(content, BorderLayout.CENTER);
    }

    private JLabel createFormRow(String label) {
        JLabel lbl = UIUtils.createFormLabel(label);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private void showEmptyTicket() {
        ticketPanel.removeAll();
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel icon = UIUtils.createLabel("🎫", new Font("Segoe UI Emoji", Font.PLAIN, 64), UIUtils.TEXT_MUTED);
        icon.setAlignmentX(CENTER_ALIGNMENT);
        JLabel msg = UIUtils.createLabel("Parking ticket will appear here", UIUtils.FONT_BODY, UIUtils.TEXT_MUTED);
        msg.setAlignmentX(CENTER_ALIGNMENT);

        center.add(Box.createVerticalGlue());
        center.add(icon);
        center.add(Box.createVerticalStrut(10));
        center.add(msg);
        center.add(Box.createVerticalGlue());

        ticketPanel.add(center, BorderLayout.CENTER);
        ticketPanel.revalidate();
        ticketPanel.repaint();
    }

    private void showTicket(ParkingRecord record) {
        ticketPanel.removeAll();
        ticketPanel.setLayout(new BorderLayout());

        JPanel ticket = new JPanel();
        ticket.setOpaque(false);
        ticket.setLayout(new BoxLayout(ticket, BoxLayout.Y_AXIS));

        // Header
        JLabel header = UIUtils.createLabel("🎫  PARKING TICKET", UIUtils.FONT_SUBTITLE, UIUtils.PRIMARY);
        header.setAlignmentX(CENTER_ALIGNMENT);
        ticket.add(Box.createVerticalStrut(10));
        ticket.add(header);
        ticket.add(Box.createVerticalStrut(5));

        JLabel brandLabel = UIUtils.createLabel("ParkWise Management System", UIUtils.FONT_SMALL, UIUtils.TEXT_MUTED);
        brandLabel.setAlignmentX(CENTER_ALIGNMENT);
        ticket.add(brandLabel);
        ticket.add(Box.createVerticalStrut(20));

        // Separator
        ticket.add(UIUtils.createSeparator());
        ticket.add(Box.createVerticalStrut(15));

        // Ticket details
        addTicketRow(ticket, "Ticket ID", record.getTicketId());
        addTicketRow(ticket, "Vehicle No.", record.getVehicleNumber());
        addTicketRow(ticket, "Owner", record.getOwnerName());
        addTicketRow(ticket, "Phone", record.getPhone());
        addTicketRow(ticket, "Vehicle Type", record.getVehicleType());
        addTicketRow(ticket, "Slot No.", record.getSlotNumber());
        addTicketRow(ticket, "Entry Time", record.getEntryTime().format(dtf));

        ticket.add(Box.createVerticalStrut(10));
        ticket.add(UIUtils.createSeparator());
        ticket.add(Box.createVerticalStrut(10));

        // Rate info
        String type = record.getVehicleType();
        double rate = "Car".equals(type) ? service.getCarRate() : service.getBikeRate();
        JLabel rateLabel = UIUtils.createLabel("Rate: ₹" + String.format("%.0f", rate) + "/hour", UIUtils.FONT_BODY_BOLD, UIUtils.WARNING);
        rateLabel.setAlignmentX(CENTER_ALIGNMENT);
        ticket.add(rateLabel);

        ticket.add(Box.createVerticalStrut(15));

        // Status badge
        JLabel status = UIUtils.createBadge("✅  VEHICLE PARKED SUCCESSFULLY", UIUtils.SUCCESS);
        status.setAlignmentX(CENTER_ALIGNMENT);
        ticket.add(status);

        ticketPanel.add(ticket, BorderLayout.CENTER);
        ticketPanel.revalidate();
        ticketPanel.repaint();
    }

    private void addTicketRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = UIUtils.createLabel(label, UIUtils.FONT_BODY, UIUtils.TEXT_SECONDARY);
        JLabel val = UIUtils.createLabel(value, UIUtils.FONT_BODY_BOLD, UIUtils.TEXT_PRIMARY);
        val.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        panel.add(row);
        panel.add(Box.createVerticalStrut(6));
    }

    private void parkVehicle() {
        String vehicleNum = UIUtils.getFieldText(vehicleNumField, "e.g., KA01AB1234").toUpperCase();
        String owner = UIUtils.getFieldText(ownerField, "e.g., Rahul Sharma");
        String phone = UIUtils.getFieldText(phoneField, "e.g., 9876543210");
        String type = (String) typeCombo.getSelectedItem();

        // Validation
        if (vehicleNum.isEmpty()) {
            UIUtils.showError(this, "Please enter vehicle number.");
            vehicleNumField.requestFocus();
            return;
        }
        if (!vehicleNum.matches("[A-Z]{2}\\d{2}[A-Z]{1,2}\\d{4}")) {
            UIUtils.showError(this, "Invalid vehicle number format.\nExpected: KA01AB1234");
            vehicleNumField.requestFocus();
            return;
        }
        if (owner.isEmpty()) {
            UIUtils.showError(this, "Please enter owner name.");
            ownerField.requestFocus();
            return;
        }
        if (phone.isEmpty() || !phone.matches("\\d{10}")) {
            UIUtils.showError(this, "Please enter a valid 10-digit phone number.");
            phoneField.requestFocus();
            return;
        }

        // Check if already parked
        if (service.getActiveByVehicleNumber(vehicleNum) != null) {
            UIUtils.showError(this, "This vehicle is already parked!");
            return;
        }

        // Park the vehicle
        ParkingRecord record = service.parkVehicle(vehicleNum, owner, phone, type);
        if (record != null) {
            showTicket(record);
            UIUtils.showSuccess(this, "Vehicle parked successfully!\nTicket ID: " + record.getTicketId() + "\nSlot: " + record.getSlotNumber());
            clearForm();
            mainFrame.refreshDashboard();
        } else {
            UIUtils.showError(this, "Failed to park vehicle.\nNo available " + type + " slots or system error.");
        }
    }

    private void clearForm() {
        vehicleNumField.setText("e.g., KA01AB1234");
        vehicleNumField.setForeground(UIUtils.TEXT_MUTED);
        ownerField.setText("e.g., Rahul Sharma");
        ownerField.setForeground(UIUtils.TEXT_MUTED);
        phoneField.setText("e.g., 9876543210");
        phoneField.setForeground(UIUtils.TEXT_MUTED);
        typeCombo.setSelectedIndex(0);
    }
}
