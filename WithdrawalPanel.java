package com.atm.view;

import com.atm.controller.ATMController;
import com.atm.controller.ATMController.OperationResult;
import com.atm.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Cash Withdrawal Screen:
 * - Input field for withdrawal amount with validation checks (insufficient funds,
 *   ATM single withdrawal limit validation) + Fast Cash buttons.
 */
public class WithdrawalPanel extends JPanel {

    private final ATMController controller;
    private final ATMMainFrame mainFrame;

    private final JLabel currentBalanceLabel;
    private final JTextField amountField;

    public WithdrawalPanel(ATMController controller, ATMMainFrame mainFrame) {
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

        JLabel title = new JLabel("Cash Withdrawal");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        currentBalanceLabel = new JLabel("Available: $0.00");
        currentBalanceLabel.setFont(UITheme.FONT_BOLD);
        currentBalanceLabel.setForeground(UITheme.ACCENT_CYAN);

        header.add(title, BorderLayout.WEST);
        header.add(currentBalanceLabel, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel subtitle = new JLabel("Enter withdrawal amount or choose a Fast Cash preset below:");
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(subtitle);
        body.add(Box.createVerticalStrut(18));

        JLabel amountLbl = new JLabel("WITHDRAWAL AMOUNT ($ USD)");
        amountLbl.setFont(UITheme.FONT_BOLD);
        amountLbl.setForeground(UITheme.TEXT_MUTED);
        amountLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(amountLbl);
        body.add(Box.createVerticalStrut(8));

        amountField = UITheme.createTextField(16);
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.addActionListener(e -> handleWithdraw());
        body.add(amountField);
        body.add(Box.createVerticalStrut(18));

        // Fast Cash Presets
        JPanel fastCashPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        fastCashPanel.setOpaque(false);
        fastCashPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fastCashPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        int[] presets = {40, 100, 200, 500, 1000};
        for (int preset : presets) {
            JButton btn = UITheme.createButton("$" + preset, UITheme.SECONDARY, UITheme.SECONDARY_HOVER);
            btn.addActionListener(e -> amountField.setText(String.format("%.2f", (double) preset)));
            fastCashPanel.add(btn);
        }
        body.add(fastCashPanel);
        body.add(Box.createVerticalStrut(16));

        JLabel limitNote = new JLabel(String.format(
                "ATM Withdrawal Limit: Maximum $%,.2f per transaction. Must not exceed available balance.",
                ATMController.MAX_SINGLE_WITHDRAWAL_LIMIT));
        limitNote.setFont(new Font("SansSerif", Font.PLAIN, 12));
        limitNote.setForeground(UITheme.TEXT_MUTED);
        limitNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(limitNote);

        card.add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new GridLayout(1, 2, 16, 0));
        footer.setOpaque(false);

        JButton backBtn = UITheme.createButton("← BACK TO DASHBOARD", UITheme.SECONDARY, UITheme.SECONDARY_HOVER);
        backBtn.addActionListener(e -> mainFrame.showScreen(ATMMainFrame.SCREEN_DASHBOARD));

        JButton withdrawBtn = UITheme.createButton("DISPENSE CASH  →", UITheme.PRIMARY, UITheme.PRIMARY_HOVER);
        withdrawBtn.addActionListener(e -> handleWithdraw());

        footer.add(backBtn);
        footer.add(withdrawBtn);
        card.add(footer, BorderLayout.SOUTH);

        add(card);
    }

    private void handleWithdraw() {
        String raw = amountField.getText().trim();
        if (raw.isEmpty()) {
            UITheme.showError(this, "Please enter a withdrawal amount.");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            UITheme.showError(this, "Invalid amount format. Please enter a valid number.");
            return;
        }

        OperationResult res = controller.withdraw(amount);
        if (res.success()) {
            currentBalanceLabel.setText(String.format("Available: $%,.2f", res.updatedBalance()));
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
            currentBalanceLabel.setText(String.format("Available: $%,.2f", user.getBalance()));
        }
        amountField.setText("");
        amountField.requestFocusInWindow();
    }
}
