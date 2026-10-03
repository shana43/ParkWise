package com.parkwise;

import com.parkwise.db.DatabaseConnection;
import com.parkwise.ui.LoginFrame;
import com.parkwise.ui.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * ParkWise - Smart Parking Management System
 * 
 * Main application entry point.
 * Initializes the database connection, sets up the Look and Feel,
 * and launches the login screen.
 * 
 * @author ParkWise Team
 * @version 1.0
 */
public class ParkWiseApp {

    public static void main(String[] args) {
        // Set system look and feel enhancements
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            
            // Global UI defaults for dark theme
            UIManager.put("Panel.background", UIUtils.BG_DARK);
            UIManager.put("OptionPane.background", UIUtils.BG_CARD);
            UIManager.put("OptionPane.messageForeground", UIUtils.TEXT_PRIMARY);
            UIManager.put("OptionPane.messageFont", UIUtils.FONT_BODY);
            UIManager.put("OptionPane.buttonFont", UIUtils.FONT_BODY_BOLD);
            UIManager.put("Button.background", UIUtils.PRIMARY);
            UIManager.put("Button.foreground", UIUtils.TEXT_PRIMARY);
            UIManager.put("Button.font", UIUtils.FONT_BODY);
            UIManager.put("ComboBox.background", UIUtils.BG_INPUT);
            UIManager.put("ComboBox.foreground", UIUtils.TEXT_PRIMARY);
            UIManager.put("ComboBox.selectionBackground", UIUtils.PRIMARY);
            UIManager.put("ComboBox.selectionForeground", UIUtils.TEXT_PRIMARY);
            UIManager.put("TextField.background", UIUtils.BG_INPUT);
            UIManager.put("TextField.foreground", UIUtils.TEXT_PRIMARY);
            UIManager.put("TextField.caretForeground", UIUtils.TEXT_PRIMARY);
            UIManager.put("PasswordField.background", UIUtils.BG_INPUT);
            UIManager.put("PasswordField.foreground", UIUtils.TEXT_PRIMARY);
            UIManager.put("PasswordField.caretForeground", UIUtils.TEXT_PRIMARY);
            UIManager.put("ScrollBar.background", UIUtils.BG_CARD);
            UIManager.put("ScrollBar.thumb", UIUtils.BG_HOVER);
            UIManager.put("ScrollBar.track", UIUtils.BG_CARD);
            UIManager.put("Table.background", UIUtils.BG_CARD);
            UIManager.put("Table.foreground", UIUtils.TEXT_PRIMARY);
            UIManager.put("Table.selectionBackground", UIUtils.PRIMARY);
            UIManager.put("Table.selectionForeground", UIUtils.TEXT_PRIMARY);
            UIManager.put("Table.gridColor", UIUtils.BORDER);
            UIManager.put("TableHeader.background", UIUtils.BG_TABLE_HEADER);
            UIManager.put("TableHeader.foreground", UIUtils.TEXT_PRIMARY);
            UIManager.put("ScrollPane.background", UIUtils.BG_CARD);
            UIManager.put("Viewport.background", UIUtils.BG_CARD);
            UIManager.put("List.background", UIUtils.BG_INPUT);
            UIManager.put("List.foreground", UIUtils.TEXT_PRIMARY);
            UIManager.put("List.selectionBackground", UIUtils.PRIMARY);
            UIManager.put("List.selectionForeground", UIUtils.TEXT_PRIMARY);

            // Anti-aliased text
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

        } catch (Exception e) {
            System.err.println("Error setting up UI: " + e.getMessage());
        }

        // Launch application on EDT
        SwingUtilities.invokeLater(() -> {
            // Show splash/loading while testing connection
            JDialog splash = createSplashScreen();
            splash.setVisible(true);

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    // Initialize database
                    DatabaseConnection.initializeDatabase();
                    return DatabaseConnection.testConnection();
                }

                @Override
                protected void done() {
                    splash.dispose();
                    try {
                        boolean connected = get();
                        if (connected) {
                            new LoginFrame();
                        } else {
                            showConnectionError();
                        }
                    } catch (Exception ex) {
                        showConnectionError();
                    }
                }
            };
            worker.execute();
        });
    }

    private static JDialog createSplashScreen() {
        JDialog splash = new JDialog();
        splash.setUndecorated(true);
        splash.setSize(400, 250);
        splash.setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 17, 26), getWidth(), getHeight(), new Color(30, 35, 60));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Border
                g2.setColor(new Color(99, 102, 241, 60));
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(Box.createVerticalGlue());

        JLabel icon = new JLabel("🅿️");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(icon);

        panel.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("ParkWise");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);

        panel.add(Box.createVerticalStrut(5));

        JLabel subtitle = new JLabel("Connecting to database...");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(148, 163, 184));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitle);

        panel.add(Box.createVerticalStrut(20));

        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        progress.setPreferredSize(new Dimension(200, 4));
        progress.setMaximumSize(new Dimension(200, 4));
        progress.setForeground(new Color(99, 102, 241));
        progress.setBackground(new Color(35, 40, 60));
        progress.setBorderPainted(false);
        progress.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(progress);

        panel.add(Box.createVerticalGlue());

        splash.setContentPane(panel);
        return splash;
    }

    private static void showConnectionError() {
        JOptionPane.showMessageDialog(null,
                "Unable to connect to MySQL database.\n\n" +
                        "Please ensure:\n" +
                        "1. MySQL server is running\n" +
                        "2. Database 'parkwise' exists (run schema.sql)\n" +
                        "3. Check credentials in DatabaseConnection.java\n\n" +
                        "Default: root / (empty password) @ localhost:3306",
                "Database Connection Error",
                JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
