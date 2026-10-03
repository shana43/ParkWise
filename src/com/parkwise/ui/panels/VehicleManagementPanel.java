package com.parkwise.ui.panels;

import com.parkwise.model.Vehicle;
import com.parkwise.service.ParkingService;
import com.parkwise.ui.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Vehicle Management panel for CRUD operations on vehicle records.
 * Supports Add, Edit, Delete, and Search functionality.
 */
public class VehicleManagementPanel extends JPanel {

    private final ParkingService service;
    private JTable vehicleTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JTextField numField, nameField, phoneField;
    private JComboBox<String> typeCombo;
    private JButton addBtn, updateBtn, deleteBtn, clearBtn;
    private int selectedVehicleId = -1;

    public VehicleManagementPanel(ParkingService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 15));
        setBackground(UIUtils.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        buildUI();
    }

    private void buildUI() {
        add(UIUtils.createSectionHeader("Vehicle Management", "Add, edit, delete and search vehicle records"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(15, 0));
        content.setOpaque(false);

        // Left: Form
        JPanel formCard = UIUtils.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setPreferredSize(new Dimension(320, 0));

        JLabel formTitle = UIUtils.createLabel("📝  Vehicle Form", UIUtils.FONT_HEADING, UIUtils.TEXT_PRIMARY);
        formTitle.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(formTitle);
        formCard.add(Box.createVerticalStrut(20));

        // Vehicle Number
        JLabel numLabel = UIUtils.createFormLabel("Vehicle Number");
        numLabel.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(numLabel);
        numField = UIUtils.createTextField("e.g., KA01AB1234");
        numField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        numField.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(numField);
        formCard.add(Box.createVerticalStrut(12));

        // Owner Name
        JLabel nameLabel = UIUtils.createFormLabel("Owner Name");
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(nameLabel);
        nameField = UIUtils.createTextField("e.g., Rahul Sharma");
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        nameField.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(nameField);
        formCard.add(Box.createVerticalStrut(12));

        // Phone
        JLabel phoneLabel = UIUtils.createFormLabel("Phone Number");
        phoneLabel.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(phoneLabel);
        phoneField = UIUtils.createTextField("e.g., 9876543210");
        phoneField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        phoneField.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(phoneField);
        formCard.add(Box.createVerticalStrut(12));

        // Type
        JLabel typeLabel = UIUtils.createFormLabel("Vehicle Type");
        typeLabel.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(typeLabel);
        typeCombo = UIUtils.createComboBox(new String[]{"Car", "Bike"});
        typeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        typeCombo.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(typeCombo);
        formCard.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        btnPanel.setAlignmentX(LEFT_ALIGNMENT);

        addBtn = UIUtils.createButton("➕ Add", UIUtils.SUCCESS);
        addBtn.addActionListener(e -> addVehicle());

        updateBtn = UIUtils.createButton("✏️ Update", UIUtils.PRIMARY);
        updateBtn.setEnabled(false);
        updateBtn.addActionListener(e -> updateVehicle());

        deleteBtn = UIUtils.createButton("🗑️ Delete", UIUtils.DANGER);
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(e -> deleteVehicle());

        clearBtn = UIUtils.createButton("🔄 Clear", UIUtils.BG_HOVER);
        clearBtn.addActionListener(e -> clearForm());

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        formCard.add(btnPanel);
        content.add(formCard, BorderLayout.WEST);

        // Right: Search + Table
        JPanel rightPanel = new JPanel(new BorderLayout(0, 12));
        rightPanel.setOpaque(false);

        // Search bar
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);
        searchBar.setPreferredSize(new Dimension(0, 42));

        searchField = UIUtils.createTextField("Search vehicles...");
        JButton searchBtn = UIUtils.createButton("🔍 Search", UIUtils.PRIMARY);
        searchBtn.setPreferredSize(new Dimension(110, 40));
        searchBtn.addActionListener(e -> searchVehicles());
        searchField.addActionListener(e -> searchVehicles());

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(searchBtn, BorderLayout.EAST);
        rightPanel.add(searchBar, BorderLayout.NORTH);

        // Table
        JPanel tableCard = UIUtils.createCard();
        tableCard.setLayout(new BorderLayout());

        String[] columns = {"ID", "Vehicle No.", "Owner Name", "Phone", "Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        vehicleTable = new JTable(tableModel);
        UIUtils.styleTable(vehicleTable);

        // Hide ID column visually but keep data
        vehicleTable.getColumnModel().getColumn(0).setMaxWidth(0);
        vehicleTable.getColumnModel().getColumn(0).setMinWidth(0);
        vehicleTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Selection listener
        vehicleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = vehicleTable.getSelectedRow();
                if (row >= 0) {
                    loadVehicleToForm(row);
                }
            }
        });

        tableCard.add(UIUtils.createStyledScrollPane(vehicleTable), BorderLayout.CENTER);
        rightPanel.add(tableCard, BorderLayout.CENTER);

        content.add(rightPanel, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        loadAllVehicles();
    }

    private void loadAllVehicles() {
        tableModel.setRowCount(0);
        List<Vehicle> vehicles = service.getAllVehicles();
        for (Vehicle v : vehicles) {
            tableModel.addRow(new Object[]{
                    v.getId(), v.getVehicleNumber(), v.getOwnerName(), v.getPhone(), v.getVehicleType()
            });
        }
    }

    private void searchVehicles() {
        String keyword = UIUtils.getFieldText(searchField, "Search vehicles...");
        tableModel.setRowCount(0);

        List<Vehicle> vehicles;
        if (keyword.isEmpty()) {
            vehicles = service.getAllVehicles();
        } else {
            vehicles = service.searchVehicles(keyword);
        }

        for (Vehicle v : vehicles) {
            tableModel.addRow(new Object[]{
                    v.getId(), v.getVehicleNumber(), v.getOwnerName(), v.getPhone(), v.getVehicleType()
            });
        }

        if (vehicles.isEmpty()) {
            UIUtils.showWarning(this, "No vehicles found.");
        }
    }

    private void loadVehicleToForm(int row) {
        selectedVehicleId = (int) tableModel.getValueAt(row, 0);
        numField.setText((String) tableModel.getValueAt(row, 1));
        numField.setForeground(UIUtils.TEXT_PRIMARY);
        nameField.setText((String) tableModel.getValueAt(row, 2));
        nameField.setForeground(UIUtils.TEXT_PRIMARY);
        phoneField.setText((String) tableModel.getValueAt(row, 3));
        phoneField.setForeground(UIUtils.TEXT_PRIMARY);
        typeCombo.setSelectedItem(tableModel.getValueAt(row, 4));

        addBtn.setEnabled(false);
        updateBtn.setEnabled(true);
        deleteBtn.setEnabled(true);
    }

    private void addVehicle() {
        String num = UIUtils.getFieldText(numField, "e.g., KA01AB1234").toUpperCase();
        String name = UIUtils.getFieldText(nameField, "e.g., Rahul Sharma");
        String phone = UIUtils.getFieldText(phoneField, "e.g., 9876543210");
        String type = (String) typeCombo.getSelectedItem();

        if (!validateForm(num, name, phone)) return;

        Vehicle vehicle = new Vehicle(num, name, phone, type);
        if (service.addVehicle(vehicle)) {
            UIUtils.showSuccess(this, "Vehicle added successfully!");
            clearForm();
            loadAllVehicles();
        } else {
            UIUtils.showError(this, "Failed to add vehicle.");
        }
    }

    private void updateVehicle() {
        if (selectedVehicleId < 0) return;

        String num = UIUtils.getFieldText(numField, "e.g., KA01AB1234").toUpperCase();
        String name = UIUtils.getFieldText(nameField, "e.g., Rahul Sharma");
        String phone = UIUtils.getFieldText(phoneField, "e.g., 9876543210");
        String type = (String) typeCombo.getSelectedItem();

        if (!validateForm(num, name, phone)) return;

        Vehicle vehicle = new Vehicle(selectedVehicleId, num, name, phone, type);
        if (service.updateVehicle(vehicle)) {
            UIUtils.showSuccess(this, "Vehicle updated successfully!");
            clearForm();
            loadAllVehicles();
        } else {
            UIUtils.showError(this, "Failed to update vehicle.");
        }
    }

    private void deleteVehicle() {
        if (selectedVehicleId < 0) return;

        if (UIUtils.showConfirm(this, "Are you sure you want to delete this vehicle record?")) {
            if (service.deleteVehicle(selectedVehicleId)) {
                UIUtils.showSuccess(this, "Vehicle deleted successfully!");
                clearForm();
                loadAllVehicles();
            } else {
                UIUtils.showError(this, "Failed to delete vehicle.");
            }
        }
    }

    private boolean validateForm(String num, String name, String phone) {
        if (num.isEmpty()) {
            UIUtils.showError(this, "Please enter vehicle number.");
            return false;
        }
        if (!num.matches("[A-Z]{2}\\d{2}[A-Z]{1,2}\\d{4}")) {
            UIUtils.showError(this, "Invalid vehicle number format.\nExpected: KA01AB1234");
            return false;
        }
        if (name.isEmpty()) {
            UIUtils.showError(this, "Please enter owner name.");
            return false;
        }
        if (phone.isEmpty() || !phone.matches("\\d{10}")) {
            UIUtils.showError(this, "Please enter a valid 10-digit phone number.");
            return false;
        }
        return true;
    }

    private void clearForm() {
        selectedVehicleId = -1;
        numField.setText("e.g., KA01AB1234");
        numField.setForeground(UIUtils.TEXT_MUTED);
        nameField.setText("e.g., Rahul Sharma");
        nameField.setForeground(UIUtils.TEXT_MUTED);
        phoneField.setText("e.g., 9876543210");
        phoneField.setForeground(UIUtils.TEXT_MUTED);
        typeCombo.setSelectedIndex(0);
        vehicleTable.clearSelection();

        addBtn.setEnabled(true);
        updateBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
    }
}
