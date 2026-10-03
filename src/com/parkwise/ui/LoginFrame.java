package com.parkwise.ui;

import com.parkwise.service.ParkingService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Login screen for admin authentication.
 * Features a modern dark-themed login form with animated gradient background.
 */
public class LoginFrame extends JFrame {

    private final ParkingService service;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private JLabel statusLabel;

    public LoginFrame() {
        this.service = new ParkingService();
        setTitle("ParkWise - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(false);

        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // Left side - Branding panel
        JPanel brandPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(79, 70, 229), getWidth(), getHeight(), new Color(139, 92, 246));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative circles
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillOval(-50, -50, 300, 300);
                g2.fillOval(getWidth() - 150, getHeight() - 200, 250, 250);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(100, getHeight() - 100, 200, 200);

                g2.dispose();
            }
        };
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));

        // Brand content
        JPanel brandContent = new JPanel();
        brandContent.setOpaque(false);
        brandContent.setLayout(new BoxLayout(brandContent, BoxLayout.Y_AXIS));

        brandContent.add(Box.createVerticalGlue());

        JLabel iconLabel = new JLabel("🅿️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        iconLabel.setAlignmentX(CENTER_ALIGNMENT);
        brandContent.add(iconLabel);

        brandContent.add(Box.createVerticalStrut(15));

        JLabel titleLabel = new JLabel("ParkWise");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        brandContent.add(titleLabel);

        brandContent.add(Box.createVerticalStrut(8));

        JLabel tagLine = new JLabel("Smart Parking Management System");
        tagLine.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tagLine.setForeground(new Color(255, 255, 255, 200));
        tagLine.setAlignmentX(CENTER_ALIGNMENT);
        brandContent.add(tagLine);

        brandContent.add(Box.createVerticalStrut(30));

        // Feature list
        String[] features = {"✓ Real-time Slot Management", "✓ Automated Billing & Receipts", "✓ Detailed Analytics & Reports"};
        for (String feature : features) {
            JLabel fLabel = new JLabel(feature);
            fLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            fLabel.setForeground(new Color(255, 255, 255, 180));
            fLabel.setAlignmentX(CENTER_ALIGNMENT);
            brandContent.add(fLabel);
            brandContent.add(Box.createVerticalStrut(6));
        }

        brandContent.add(Box.createVerticalGlue());

        brandPanel.add(brandContent);
        mainPanel.add(brandPanel);

        // Right side - Login form
        JPanel loginPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(UIUtils.BG_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        loginPanel.setLayout(new GridBagLayout());

        JPanel formWrapper = new JPanel();
        formWrapper.setOpaque(false);
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setPreferredSize(new Dimension(320, 380));

        // Login header
        JLabel loginTitle = UIUtils.createLabel("Welcome Back", UIUtils.FONT_TITLE, UIUtils.TEXT_PRIMARY);
        loginTitle.setAlignmentX(LEFT_ALIGNMENT);
        formWrapper.add(loginTitle);

        JLabel loginSub = UIUtils.createLabel("Sign in to your admin account", UIUtils.FONT_BODY, UIUtils.TEXT_SECONDARY);
        loginSub.setAlignmentX(LEFT_ALIGNMENT);
        formWrapper.add(loginSub);
        formWrapper.add(Box.createVerticalStrut(30));

        // Username
        JLabel userLabel = UIUtils.createFormLabel("Username");
        userLabel.setAlignmentX(LEFT_ALIGNMENT);
        formWrapper.add(userLabel);
        formWrapper.add(Box.createVerticalStrut(5));
        usernameField = UIUtils.createTextField("Enter username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        usernameField.setAlignmentX(LEFT_ALIGNMENT);
        formWrapper.add(usernameField);
        formWrapper.add(Box.createVerticalStrut(18));

        // Password
        JLabel passLabel = UIUtils.createFormLabel("Password");
        passLabel.setAlignmentX(LEFT_ALIGNMENT);
        formWrapper.add(passLabel);
        formWrapper.add(Box.createVerticalStrut(5));
        passwordField = UIUtils.createPasswordField("Enter password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        passwordField.setAlignmentX(LEFT_ALIGNMENT);
        formWrapper.add(passwordField);
        formWrapper.add(Box.createVerticalStrut(25));

        // Login button
        loginBtn = UIUtils.createButton("Sign In →", UIUtils.PRIMARY);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        loginBtn.setAlignmentX(LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> handleLogin());
        formWrapper.add(loginBtn);
        formWrapper.add(Box.createVerticalStrut(12));

        // Status label
        statusLabel = UIUtils.createLabel("", UIUtils.FONT_SMALL, UIUtils.DANGER);
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        formWrapper.add(statusLabel);

        formWrapper.add(Box.createVerticalStrut(25));

        // Default credentials hint
        JPanel hintPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(99, 102, 241, 20));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };
        hintPanel.setOpaque(false);
        hintPanel.setLayout(new BoxLayout(hintPanel, BoxLayout.Y_AXIS));
        hintPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        hintPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        hintPanel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel hintLabel = UIUtils.createLabel("Default Credentials:", UIUtils.FONT_SMALL, UIUtils.TEXT_MUTED);
        hintLabel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel credLabel = UIUtils.createLabel("Username: admin  |  Password: admin123", UIUtils.FONT_SMALL, UIUtils.PRIMARY);
        credLabel.setAlignmentX(LEFT_ALIGNMENT);

        hintPanel.add(hintLabel);
        hintPanel.add(credLabel);
        formWrapper.add(hintPanel);

        loginPanel.add(formWrapper);
        mainPanel.add(loginPanel);

        setContentPane(mainPanel);

        // Enter key support
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });

        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
        });
    }

    private void handleLogin() {
        String username = UIUtils.getFieldText(usernameField, "Enter username");
        String rawPassword = String.valueOf(passwordField.getPassword());
        String password = rawPassword.equals("Enter password") ? "" : rawPassword;

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("⚠️  Please enter both username and password.");
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in...");
        statusLabel.setText("");

        // Run authentication in background
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return service.authenticateAdmin(username, password);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        String adminName = service.getAdminName(username);
                        dispose();
                        SwingUtilities.invokeLater(() -> new MainFrame(adminName));
                    } else {
                        statusLabel.setText("❌  Invalid username or password.");
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Sign In →");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("❌  Connection error. Check database.");
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Sign In →");
                }
            }
        };
        worker.execute();
    }
}
