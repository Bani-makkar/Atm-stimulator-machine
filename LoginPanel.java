package com.atm.view;

import com.atm.controller.ATMController;
import com.atm.controller.ATMController.OperationResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Login Screen View:
 * - Inputs for Account Number and 4-digit PIN with validation and error handling.
 * - Includes an interactive ATM PIN Keypad and Quick-Fill demo account buttons.
 */
public class LoginPanel extends JPanel {

    private final ATMController controller;
    private final ATMMainFrame mainFrame;

    private final JTextField accountField;
    private final JPasswordField pinField;
    private final JLabel feedbackLabel;

    public LoginPanel(ATMController controller, ATMMainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_DARK);

        JPanel card = UITheme.createCardPanel(new BorderLayout(28, 0));
        card.setBorder(new EmptyBorder(32, 36, 32, 36));
        card.setPreferredSize(new Dimension(780, 470));

        // Left side: Credentials & Demo Quick-Select
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome to Horizon ATM");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Insert card or enter your Account Number and 4-digit PIN");
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(title);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(subtitle);
        formPanel.add(Box.createVerticalStrut(26));

        JLabel accLabel = new JLabel("ACCOUNT NUMBER");
        accLabel.setFont(UITheme.FONT_BOLD);
        accLabel.setForeground(UITheme.TEXT_MUTED);
        accLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        accountField = UITheme.createTextField(16);
        accountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        accountField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(accLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(accountField);
        formPanel.add(Box.createVerticalStrut(18));

        JLabel pinLabel = new JLabel("4-DIGIT SECRET PIN");
        pinLabel.setFont(UITheme.FONT_BOLD);
        pinLabel.setForeground(UITheme.TEXT_MUTED);
        pinLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        pinField = UITheme.createPasswordField(8);
        pinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        pinField.setAlignmentX(Component.LEFT_ALIGNMENT);
        pinField.addActionListener(e -> handleLogin());

        formPanel.add(pinLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(pinField);
        formPanel.add(Box.createVerticalStrut(14));

        feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(UITheme.FONT_BODY);
        feedbackLabel.setForeground(UITheme.DANGER);
        feedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(feedbackLabel);
        formPanel.add(Box.createVerticalStrut(12));

        JButton loginBtn = UITheme.createButton("AUTHENTICATE & ENTER ATM  →", UITheme.PRIMARY, UITheme.PRIMARY_HOVER);
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        loginBtn.addActionListener(e -> handleLogin());
        formPanel.add(loginBtn);

        formPanel.add(Box.createVerticalStrut(22));

        // Demo quick-fill bar
        JPanel demoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        demoBar.setOpaque(false);
        demoBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel demoTitle = new JLabel("Quick Fill Demo:");
        demoTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        demoTitle.setForeground(UITheme.TEXT_MUTED);
        demoBar.add(demoTitle);

        demoBar.add(createQuickFillButton("Aarav (Savings)", "1001001", "1234"));
        demoBar.add(createQuickFillButton("Priya (Current)", "1001002", "4321"));
        demoBar.add(createQuickFillButton("Rohan (Savings)", "1001003", "9999"));

        formPanel.add(demoBar);

        // Right side: Interactive Numeric Keypad
        JPanel keypadPanel = createNumericKeypad();

        card.add(formPanel, BorderLayout.CENTER);
        card.add(keypadPanel, BorderLayout.EAST);

        add(card);
    }

    private JButton createQuickFillButton(String label, String acc, String pin) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setForeground(UITheme.ACCENT_CYAN);
        btn.setBackground(UITheme.BG_DARK);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        btn.addActionListener(e -> {
            accountField.setText(acc);
            pinField.setText(pin);
            feedbackLabel.setText("Demo credentials loaded. Click Authenticate.");
            feedbackLabel.setForeground(UITheme.SUCCESS);
        });
        return btn;
    }

    private JPanel createNumericKeypad() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(240, 340));

        JLabel keypadTitle = new JLabel("ATM PIN PAD", SwingConstants.CENTER);
        keypadTitle.setFont(UITheme.FONT_BOLD);
        keypadTitle.setForeground(UITheme.TEXT_MUTED);
        wrapper.add(keypadTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(4, 3, 10, 10));
        grid.setOpaque(false);

        String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "CLR", "0", "DEL"};
        for (String key : keys) {
            Color base = switch (key) {
                case "CLR" -> UITheme.DANGER;
                case "DEL" -> UITheme.WARNING;
                default -> UITheme.SECONDARY;
            };
            Color hover = switch (key) {
                case "CLR" -> UITheme.DANGER_HOVER;
                case "DEL" -> UITheme.WARNING.darker();
                default -> UITheme.SECONDARY_HOVER;
            };

            JButton btn = UITheme.createButton(key, base, hover);
            btn.setFont(new Font("Monospaced", Font.BOLD, 16));
            btn.addActionListener(e -> onKeypadPress(key));
            grid.add(btn);
        }

        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private void onKeypadPress(String key) {
        String currentPin = new String(pinField.getPassword());
        switch (key) {
            case "CLR" -> pinField.setText("");
            case "DEL" -> {
                if (!currentPin.isEmpty()) {
                    pinField.setText(currentPin.substring(0, currentPin.length() - 1));
                }
            }
            default -> {
                if (currentPin.length() < 4) {
                    pinField.setText(currentPin + key);
                }
            }
        }
    }

    private void handleLogin() {
        String accountNumber = accountField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();

        OperationResult result = controller.login(accountNumber, pin);
        if (result.success()) {
            feedbackLabel.setText(" ");
            pinField.setText("");
            mainFrame.showScreen(ATMMainFrame.SCREEN_DASHBOARD);
        } else {
            feedbackLabel.setForeground(UITheme.DANGER);
            feedbackLabel.setText(result.message());
            UITheme.showError(this, result.message());
        }
    }

    public void onShow() {
        pinField.setText("");
        feedbackLabel.setText(" ");
        accountField.requestFocusInWindow();
    }
}
