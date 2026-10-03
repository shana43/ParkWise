package com.parkwise.ui.panels;

import com.parkwise.model.ParkingSlot;
import com.parkwise.service.ParkingService;
import com.parkwise.ui.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Parking Slots panel showing visual grid layout of all slots
 * with live Available/Occupied status and filtering by type.
 */
public class ParkingSlotsPanel extends JPanel {

    private final ParkingService service;
    private JPanel slotsGrid;
    private JComboBox<String> filterCombo;
    private JLabel bikeCountLabel, carCountLabel;

    public ParkingSlotsPanel(ParkingService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 20));
        setBackground(UIUtils.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        buildUI();
    }

    private void buildUI() {
        add(UIUtils.createSectionHeader("Parking Slots", "Visual overview of all parking slots"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setOpaque(false);

        // Top bar with filter and stats
        JPanel topBar = new JPanel(new BorderLayout(15, 0));
        topBar.setOpaque(false);

        // Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);
        filterPanel.add(UIUtils.createLabel("Filter:", UIUtils.FONT_BODY_BOLD, UIUtils.TEXT_SECONDARY));
        filterCombo = UIUtils.createComboBox(new String[]{"All", "Bike", "Car"});
        filterCombo.setPreferredSize(new Dimension(150, 38));
        filterCombo.addActionListener(e -> refreshSlots());
        filterPanel.add(filterCombo);

        JButton refreshBtn = UIUtils.createButton("🔄 Refresh", UIUtils.PRIMARY);
        refreshBtn.setPreferredSize(new Dimension(120, 38));
        refreshBtn.addActionListener(e -> refreshSlots());
        filterPanel.add(refreshBtn);

        topBar.add(filterPanel, BorderLayout.WEST);

        // Slot counts
        JPanel countsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        countsPanel.setOpaque(false);

        bikeCountLabel = UIUtils.createBadge("🏍️ Bikes: 0/0", UIUtils.ACCENT_CYAN);
        carCountLabel = UIUtils.createBadge("🚗 Cars: 0/0", UIUtils.ACCENT_ORANGE);
        JLabel legend1 = UIUtils.createBadge("🟢 Available", UIUtils.SUCCESS);
        JLabel legend2 = UIUtils.createBadge("🔴 Occupied", UIUtils.DANGER);

        countsPanel.add(bikeCountLabel);
        countsPanel.add(carCountLabel);
        countsPanel.add(legend1);
        countsPanel.add(legend2);

        topBar.add(countsPanel, BorderLayout.EAST);
        content.add(topBar, BorderLayout.NORTH);

        // Slots grid
        JPanel cardWrapper = UIUtils.createCard();
        slotsGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        slotsGrid.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(slotsGrid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        cardWrapper.add(scrollPane, BorderLayout.CENTER);
        content.add(cardWrapper, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
        refreshSlots();
    }

    public void refreshSlots() {
        slotsGrid.removeAll();

        String filter = (String) filterCombo.getSelectedItem();
        List<ParkingSlot> slots;

        if ("All".equals(filter)) {
            slots = service.getAllSlots();
        } else {
            slots = service.getSlotsByType(filter);
        }

        for (ParkingSlot slot : slots) {
            slotsGrid.add(createSlotTile(slot));
        }

        // Update counts
        int bikeAvail = service.getAvailableBikeSlots();
        int bikeTotal = service.getTotalBikeSlots();
        int carAvail = service.getAvailableCarSlots();
        int carTotal = service.getTotalCarSlots();

        bikeCountLabel.setText("🏍️ Bikes: " + bikeAvail + "/" + bikeTotal);
        carCountLabel.setText("🚗 Cars: " + carAvail + "/" + carTotal);

        slotsGrid.revalidate();
        slotsGrid.repaint();
    }

    private JPanel createSlotTile(ParkingSlot slot) {
        boolean available = slot.isAvailable();
        Color bgColor = available ? new Color(22, 101, 52, 60) : new Color(153, 27, 27, 60);
        Color borderColor = available ? UIUtils.SUCCESS : UIUtils.DANGER;
        Color textColor = available ? UIUtils.SUCCESS : UIUtils.DANGER;

        JPanel tile = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));

                // Border
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 12, 12));

                g2.dispose();
            }
        };
        tile.setOpaque(false);
        tile.setPreferredSize(new Dimension(110, 80));
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Slot number
        JLabel numLabel = UIUtils.createLabel(slot.getSlotNumber(), UIUtils.FONT_BODY_BOLD, UIUtils.TEXT_PRIMARY);
        numLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Type icon
        String icon = "Bike".equals(slot.getSlotType()) ? "🏍️" : "🚗";
        JLabel typeLabel = UIUtils.createLabel(icon, new Font("Segoe UI Emoji", Font.PLAIN, 16), UIUtils.TEXT_PRIMARY);
        typeLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Status
        String statusText = available ? "Available" : "Occupied";
        JLabel statusLabel = UIUtils.createLabel(statusText, UIUtils.FONT_TINY, textColor);
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        tile.add(numLabel);
        tile.add(Box.createVerticalStrut(2));
        tile.add(typeLabel);
        tile.add(Box.createVerticalStrut(2));
        tile.add(statusLabel);

        return tile;
    }
}
