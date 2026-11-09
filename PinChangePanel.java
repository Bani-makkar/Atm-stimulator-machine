package com.atm.view;

import com.atm.controller.ATMController;
import com.atm.controller.ATMController.OperationResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * PIN Change Screen:
 * - Requires old PIN verification, new 4-digit PIN entry, and confirmation matching
 *   before updating the SHA-256 hashed PIN in the database.
 */
public class PinChangePanel extends JPanel {

    private final ATMController controller;
    private final ATMMainFrame mainFrame;

    private final JPasswordField oldPinField;
    private final JPasswordField newPinField;
    private final JPasswordField confirmPinField;

    public PinChangePanel(ATMController controller, ATMMainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_DARK);

        JPanel card = UITheme.createCardPanel(new BorderLayout(0, 20));
        card.setBorder(new EmptyBorder(32, 40, 32, 40));
        card.setPreferredSize(new Dimension(620, 460));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Change 4-Digit ATM PIN");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel badge = new JLabel("SHA-256 Encrypted");
        badge.setFont(UITheme.FONT_BOLD);
        badge.setForeground(UITheme.ACCENT_CYAN);

        header.add(title, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(createFieldLabel("CURRENT 4-DIGIT PIN"));
        body.add(Box.createVerticalStrut(6));
        oldPinField = UITheme.createPasswordField(8);
        oldPinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        oldPinField.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(oldPinField);
        body.add(Box.createVerticalStrut(16));

        body.add(createFieldLabel("NEW 4-DIGIT PIN"));
        body.add(Box.createVerticalStrut(6));
        newPinField = UITheme.createPasswordField(8);
        newPinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        newPinField.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(newPinField);
        body.add(Box.createVerticalStrut(16));

        body.add(createFieldLabel("CONFIRM NEW 4-DIGIT PIN"));
        body.add(Box.createVerticalStrut(6));
        confirmPinField = UITheme.createPasswordField(8);
        confirmPinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        confirmPinField.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmPinField.addActionListener(e -> handlePinChange());
        body.add(confirmPinField);

        card.add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new GridLayout(1, 2, 16, 0));
        footer.setOpaque(false);

        JButton backBtn = UITheme.createButton("← BACK TO DASHBOARD", UITheme.SECONDARY, UITheme.SECONDARY_HOVER);
        backBtn.addActionListener(e -> mainFrame.showScreen(ATMMainFrame.SCREEN_DASHBOARD));

        JButton updateBtn = UITheme.createButton("UPDATE PIN SECURELY  ✓", UITheme.SUCCESS, UITheme.SUCCESS_HOVER);
        updateBtn.addActionListener(e -> handlePinChange());

        footer.add(backBtn);
        footer.add(updateBtn);
        card.add(footer, BorderLayout.SOUTH);

        add(card);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void handlePinChange() {
        String oldPin = new String(oldPinField.getPassword()).trim();
        String newPin = new String(newPinField.getPassword()).trim();
        String confirmPin = new String(confirmPinField.getPassword()).trim();

        OperationResult res = controller.changePin(oldPin, newPin, confirmPin);
        if (res.success()) {
            clearFields();
            UITheme.showSuccess(this, res.message());
            mainFrame.showScreen(ATMMainFrame.SCREEN_DASHBOARD);
        } else {
            UITheme.showError(this, res.message());
        }
    }

    private void clearFields() {
        oldPinField.setText("");
        newPinField.setText("");
        confirmPinField.setText("");
    }

    public void onShow() {
        clearFields();
        oldPinField.requestFocusInWindow();
    }
}
