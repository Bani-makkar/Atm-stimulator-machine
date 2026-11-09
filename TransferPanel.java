package com.atm.view;

import com.atm.controller.ATMController;
import com.atm.controller.ATMController.OperationResult;
import com.atm.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Fund Transfer Screen:
 * - Inputs for recipient account number and transfer amount.
 * - Ensures source account has sufficient funds and target account exists.
 */
public class TransferPanel extends JPanel {

    private final ATMController controller;
    private final ATMMainFrame mainFrame;

    private final JLabel currentBalanceLabel;
    private final JTextField recipientField;
    private final JTextField amountField;

    public TransferPanel(ATMController controller, ATMMainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_DARK);

        JPanel card = UITheme.createCardPanel(new BorderLayout(0, 20));
        card.setBorder(new EmptyBorder(32, 40, 32, 40));
        card.setPreferredSize(new Dimension(660, 460));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Fund Transfer");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        currentBalanceLabel = new JLabel("Available: $0.00");
        currentBalanceLabel.setFont(UITheme.FONT_BOLD);
        currentBalanceLabel.setForeground(UITheme.ACCENT_CYAN);

        header.add(title, BorderLayout.WEST);
        header.add(currentBalanceLabel, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Form Body
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel subtitle = new JLabel("Transfer money instantly to another Horizon Bank account:");
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(subtitle);
        body.add(Box.createVerticalStrut(18));

        JLabel recipientLbl = new JLabel("RECIPIENT ACCOUNT NUMBER");
        recipientLbl.setFont(UITheme.FONT_BOLD);
        recipientLbl.setForeground(UITheme.TEXT_MUTED);
        recipientLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(recipientLbl);
        body.add(Box.createVerticalStrut(6));

        recipientField = UITheme.createTextField(16);
        recipientField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        recipientField.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(recipientField);
        body.add(Box.createVerticalStrut(16));

        JLabel amountLbl = new JLabel("TRANSFER AMOUNT ($ USD)");
        amountLbl.setFont(UITheme.FONT_BOLD);
        amountLbl.setForeground(UITheme.TEXT_MUTED);
        amountLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(amountLbl);
        body.add(Box.createVerticalStrut(6));

        amountField = UITheme.createTextField(16);
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.addActionListener(e -> handleTransfer());
        body.add(amountField);
        body.add(Box.createVerticalStrut(16));

        JLabel demoPeersHint = new JLabel(
                "Tip: Valid demo recipient accounts are 1001001 (Aarav), 1001002 (Priya), and 1001003 (Rohan).");
        demoPeersHint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        demoPeersHint.setForeground(UITheme.ACCENT_CYAN);
        demoPeersHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(demoPeersHint);

        card.add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new GridLayout(1, 2, 16, 0));
        footer.setOpaque(false);

        JButton backBtn = UITheme.createButton("← BACK TO DASHBOARD", UITheme.SECONDARY, UITheme.SECONDARY_HOVER);
        backBtn.addActionListener(e -> mainFrame.showScreen(ATMMainFrame.SCREEN_DASHBOARD));

        JButton transferBtn = UITheme.createButton("SEND TRANSFER  ⇄", UITheme.PRIMARY, UITheme.PRIMARY_HOVER);
        transferBtn.addActionListener(e -> handleTransfer());

        footer.add(backBtn);
        footer.add(transferBtn);
        card.add(footer, BorderLayout.SOUTH);

        add(card);
    }

    private void handleTransfer() {
        String recipientAcc = recipientField.getText().trim();
        String rawAmount = amountField.getText().trim();

        if (recipientAcc.isEmpty()) {
            UITheme.showError(this, "Please enter a recipient account number.");
            return;
        }
        if (rawAmount.isEmpty()) {
            UITheme.showError(this, "Please enter a transfer amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(rawAmount);
        } catch (NumberFormatException e) {
            UITheme.showError(this, "Invalid transfer amount. Please enter a valid number.");
            return;
        }

        OperationResult res = controller.transferFunds(recipientAcc, amount);
        if (res.success()) {
            currentBalanceLabel.setText(String.format("Available: $%,.2f", res.updatedBalance()));
            recipientField.setText("");
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
        recipientField.setText("");
        amountField.setText("");
        recipientField.requestFocusInWindow();
    }
}
