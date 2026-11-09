package com.atm.view;

import com.atm.controller.ATMController;
import com.atm.controller.ATMController.OperationResult;
import com.atm.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Cash Deposit Screen:
 * - Input field for deposit amount + quick denomination buttons
 * - Instant balance update and atomic database commit
 */
public class DepositPanel extends JPanel {

    private final ATMController controller;
    private final ATMMainFrame mainFrame;

    private final JLabel currentBalanceLabel;
    private final JTextField amountField;

    public DepositPanel(ATMController controller, ATMMainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_DARK);

        JPanel card = UITheme.createCardPanel(new BorderLayout(0, 20));
        card.setBorder(new EmptyBorder(32, 40, 32, 40));
        card.setPreferredSize(new Dimension(640, 440));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Cash Deposit");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        currentBalanceLabel = new JLabel("Current Balance: $0.00");
        currentBalanceLabel.setFont(UITheme.FONT_BOLD);
        currentBalanceLabel.setForeground(UITheme.SUCCESS);

        header.add(title, BorderLayout.WEST);
        header.add(currentBalanceLabel, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Form Body
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel instruction = new JLabel("Enter the amount to deposit or select a quick denomination below:");
        instruction.setFont(UITheme.FONT_SUBTITLE);
        instruction.setForeground(UITheme.TEXT_MUTED);
        instruction.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(instruction);
        body.add(Box.createVerticalStrut(18));

        JLabel amountLbl = new JLabel("DEPOSIT AMOUNT ($ USD)");
        amountLbl.setFont(UITheme.FONT_BOLD);
        amountLbl.setForeground(UITheme.TEXT_MUTED);
        amountLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(amountLbl);
        body.add(Box.createVerticalStrut(8));

        amountField = UITheme.createTextField(16);
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.addActionListener(e -> handleDeposit());
        body.add(amountField);
        body.add(Box.createVerticalStrut(18));

        // Quick Denomination Buttons
        JPanel denomPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        denomPanel.setOpaque(false);
        denomPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        denomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        int[] denominations = {50, 100, 500, 1000, 5000};
        for (int denom : denominations) {
            JButton btn = UITheme.createButton("+$" + denom, UITheme.SECONDARY, UITheme.SECONDARY_HOVER);
            btn.addActionListener(e -> addDenomination(denom));
            denomPanel.add(btn);
        }
        body.add(denomPanel);
        body.add(Box.createVerticalStrut(16));

        JLabel limitInfo = new JLabel(String.format(
                "Note: Maximum single cash deposit is $%,.2f. Funds are credited immediately.",
                ATMController.MAX_SINGLE_DEPOSIT_LIMIT));
        limitInfo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        limitInfo.setForeground(UITheme.TEXT_MUTED);
        limitInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(limitInfo);

        card.add(body, BorderLayout.CENTER);

        // Footer Buttons
        JPanel footer = new JPanel(new GridLayout(1, 2, 16, 0));
        footer.setOpaque(false);

        JButton backBtn = UITheme.createButton("← BACK TO DASHBOARD", UITheme.SECONDARY, UITheme.SECONDARY_HOVER);
        backBtn.addActionListener(e -> mainFrame.showScreen(ATMMainFrame.SCREEN_DASHBOARD));

        JButton confirmBtn = UITheme.createButton("CONFIRM DEPOSIT  ✓", UITheme.SUCCESS, UITheme.SUCCESS_HOVER);
        confirmBtn.addActionListener(e -> handleDeposit());

        footer.add(backBtn);
        footer.add(confirmBtn);
        card.add(footer, BorderLayout.SOUTH);

        add(card);
    }

    private void addDenomination(int delta) {
        double current = 0.0;
        try {
            String text = amountField.getText().trim();
            if (!text.isEmpty()) {
                current = Double.parseDouble(text);
            }
        } catch (NumberFormatException ignored) {
        }
        amountField.setText(String.format("%.2f", current + delta));
    }

    private void handleDeposit() {
        String raw = amountField.getText().trim();
        if (raw.isEmpty()) {
            UITheme.showError(this, "Please enter a deposit amount.");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            UITheme.showError(this, "Invalid numeric amount. Please enter a valid number (e.g. 500.00).");
            return;
        }

        OperationResult res = controller.deposit(amount);
        if (res.success()) {
            currentBalanceLabel.setText(String.format("Current Balance: $%,.2f", res.updatedBalance()));
            amountField.setText("");
            UITheme.showSuccess(this, res.message());
            mainFrame.showScreen(ATMMainFrame.SCREEN_DASHBOARD);
        } else {
            UITheme.showError(this, res.message());
        }
    }

    public void onShow() {
        User user = controller.getCurrentUser();
        if (user != null) {
            currentBalanceLabel.setText(String.format("Current Balance: $%,.2f", user.getBalance()));
        }
        amountField.setText("");
        amountField.requestFocusInWindow();
    }
}
