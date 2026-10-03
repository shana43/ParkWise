package com.parkwise.ui;

import com.parkwise.service.ParkingService;
import com.parkwise.ui.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main application frame with sidebar navigation and content panels.
 * Provides the primary UI shell for the ParkWise application.
 */
public class MainFrame extends JFrame {

    private final ParkingService service;
    private final String adminName;
    private JPanel contentPanel;
    private JPanel sidebarPanel;
    private String activeMenu = "Dashboard";

    // Panel instances
    private DashboardPanel dashboardPanel;
    private VehicleEntryPanel vehicleEntryPanel;
    private VehicleExitPanel vehicleExitPanel;
    private ParkingSlotsPanel parkingSlotsPanel;
    private HistoryPanel historyPanel;
    private VehicleManagementPanel vehicleManagementPanel;
    private ReportsPanel reportsPanel;

    // Clock label
    private JLabel clockLabel;

    public MainFrame(String adminName) {
        this.adminName = adminName;
        this.service = new ParkingService();

        setTitle("ParkWise - Parking Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1366, 768);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);

        // Initialize panels
        dashboardPanel = new DashboardPanel(service);
        vehicleEntryPanel = new VehicleEntryPanel(service, this);
        vehicleExitPanel = new VehicleExitPanel(service, this);
        parkingSlotsPanel = new ParkingSlotsPanel(service);
        historyPanel = new HistoryPanel(service);
        vehicleManagementPanel = new VehicleManagementPanel(service);
        reportsPanel = new ReportsPanel(service);

        buildUI();
        startClock();
        setVisible(true);
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(UIUtils.BG_DARK);

        // Sidebar
        sidebarPanel = buildSidebar();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // Content area
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(UIUtils.BG_DARK);

        // Top bar
        rightPanel.add(buildTopBar(), BorderLayout.NORTH);

        // Content
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIUtils.BG_DARK);
        contentPanel.add(dashboardPanel, BorderLayout.CENTER);

        rightPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UIUtils.BG_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Right border line
                g2.setColor(UIUtils.BORDER);
                g2.fillRect(getWidth() - 1, 0, 1, getHeight());

                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Logo area
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
        logoPanel.setMaximumSize(new Dimension(240, 65));

        JLabel logoIcon = new JLabel("🅿️");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        JLabel logoText = UIUtils.createLabel("ParkWise", UIUtils.FONT_LOGO, UIUtils.TEXT_PRIMARY);

        logoPanel.add(logoIcon);
        logoPanel.add(logoText);

        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(UIUtils.createSeparator());
        sidebar.add(Box.createVerticalStrut(15));

        // Menu label
        JLabel menuLabel = UIUtils.createLabel("   MAIN MENU", UIUtils.FONT_TINY, UIUtils.TEXT_MUTED);
        menuLabel.setAlignmentX(LEFT_ALIGNMENT);
        menuLabel.setMaximumSize(new Dimension(240, 20));
        sidebar.add(menuLabel);
        sidebar.add(Box.createVerticalStrut(8));

        // Menu items
        refreshSidebar(sidebar);

        return sidebar;
    }

    private void refreshSidebar(JPanel sidebar) {
        // Remove only menu items (keep logo, separator, label)
        Component[] components = sidebar.getComponents();
        int keepCount = 5; // logo, strut, separator, strut, menu label, strut
        while (sidebar.getComponentCount() > keepCount) {
            sidebar.remove(keepCount);
        }

        String[][] menuItems = {
                {"📊", "Dashboard"},
                {"🚗", "Vehicle Entry"},
                {"🚪", "Vehicle Exit"},
                {"🅿️", "Parking Slots"},
                {"📜", "History"},
                {"📝", "Vehicles"},
                {"📈", "Reports"}
        };

        for (String[] item : menuItems) {
            JPanel menuBtn = UIUtils.createSidebarButton(item[0], item[1], item[1].equals(activeMenu), () -> {
                switchPanel(item[1]);
            });
            sidebar.add(menuBtn);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        // Logout section
        sidebar.add(UIUtils.createSeparator());
        sidebar.add(Box.createVerticalStrut(8));

        JPanel logoutBtn = UIUtils.createSidebarButton("🚪", "Logout", false, this::logout);
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.revalidate();
        sidebar.repaint();
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UIUtils.BG_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Bottom border
                g2.setColor(UIUtils.BORDER);
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);

                g2.dispose();
            }
        };
        topBar.setPreferredSize(new Dimension(0, 55));
        topBar.setLayout(new BorderLayout(15, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Left: Current page title
        JLabel pageTitle = UIUtils.createLabel("Dashboard", UIUtils.FONT_SUBTITLE, UIUtils.TEXT_PRIMARY);
        topBar.add(pageTitle, BorderLayout.WEST);

        // Right: Clock + Admin info
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightInfo.setOpaque(false);

        clockLabel = UIUtils.createLabel("", UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        rightInfo.add(clockLabel);

        // Admin badge
        JLabel adminBadge = new JLabel("👤 " + adminName) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(99, 102, 241, 30));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        adminBadge.setFont(UIUtils.FONT_BODY);
        adminBadge.setForeground(UIUtils.TEXT_PRIMARY);
        adminBadge.setOpaque(false);
        adminBadge.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        rightInfo.add(adminBadge);

        topBar.add(rightInfo, BorderLayout.EAST);

        return topBar;
    }

    public void switchPanel(String panelName) {
        activeMenu = panelName;
        contentPanel.removeAll();

        switch (panelName) {
            case "Dashboard":
                dashboardPanel.refreshData();
                contentPanel.add(dashboardPanel, BorderLayout.CENTER);
                break;
            case "Vehicle Entry":
                contentPanel.add(vehicleEntryPanel, BorderLayout.CENTER);
                break;
            case "Vehicle Exit":
                contentPanel.add(vehicleExitPanel, BorderLayout.CENTER);
                break;
            case "Parking Slots":
                parkingSlotsPanel.refreshSlots();
                contentPanel.add(parkingSlotsPanel, BorderLayout.CENTER);
                break;
            case "History":
                historyPanel.refreshData();
                contentPanel.add(historyPanel, BorderLayout.CENTER);
                break;
            case "Vehicles":
                contentPanel.add(vehicleManagementPanel, BorderLayout.CENTER);
                break;
            case "Reports":
                contentPanel.add(reportsPanel, BorderLayout.CENTER);
                break;
        }

        contentPanel.revalidate();
        contentPanel.repaint();

        // Refresh sidebar to highlight active item
        refreshSidebar(sidebarPanel);
    }

    public void refreshDashboard() {
        dashboardPanel.refreshData();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(LoginFrame::new);
        }
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  |  HH:mm:ss"));
            clockLabel.setText("🕐 " + time);
        });
        timer.start();
    }
}
