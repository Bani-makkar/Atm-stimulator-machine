package com.atm.view;

import com.atm.controller.ATMController;
import com.atm.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main Dashboard Screen:
 * - Displays account holder name, account number, account type badge (Savings/Current),
 *   and real-time formatted current balance.
 * - Provides intuitive ATM menu action tiles for Deposit, Withdrawal, Fund Transfer,
 *   Mini Statement, PIN Change, and Safe Card Eject (Logout).
 */
public class DashboardPanel extends JPanel {

    private final ATMController controller;
    private final ATMMainFrame mainFrame;

    private final JLabel holderNameLabel;
    private final JLabel accountMetaLabel;
    private final JLabel balanceValueLabel;
    private final JLabel accountTypeBadge;

    public DashboardPanel(ATMController controller, ATMMainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 22));
        setBackground(UITheme.BG_DARK);

        // Top Account Summary Banner Card
        JPanel summaryCard = UITheme.createCardPanel(new BorderLayout(20, 0));
        summaryCard.setBorder(new EmptyBorder(24, 30, 24, 30));

        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setOpaque(false);
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));

        JLabel welcomeCaption = new JLabel("AUTHENTICATED ACCOUNT HOLDER");
        welcomeCaption.setFont(new Font("SansSerif", Font.BOLD, 11));
        welcomeCaption.setForeground(UITheme.ACCENT_CYAN);

        holderNameLabel = new JLabel("—");
        holderNameLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        holderNameLabel.setForeground(UITheme.TEXT_PRIMARY);

        accountMetaLabel = new JLabel("Account #—");
        accountMetaLabel.setFont(UITheme.FONT_BODY);
        accountMetaLabel.setForeground(UITheme.TEXT_MUTED);

        accountTypeBadge = new JLabel("SAVINGS ACCOUNT");
        accountTypeBadge.setFont(new Font("SansSerif", Font.BOLD, 12));
        accountTypeBadge.setForeground(UITheme.SUCCESS);

        userInfoPanel.add(welcomeCaption);
        userInfoPanel.add(Box.createVerticalStrut(4));
        userInfoPanel.add(holderNameLabel);
        userInfoPanel.add(Box.createVerticalStrut(6));
        userInfoPanel.add(accountMetaLabel);
        userInfoPanel.add(Box.createVerticalStrut(4));
        userInfoPanel.add(accountTypeBadge);

        // Right side of summary: Current Balance Display
        JPanel balancePanel = new JPanel();
        balancePanel.setOpaque(false);
        balancePanel.setLayout(new BoxLayout(balancePanel, BoxLayout.Y_AXIS));

        JLabel balanceCaption = new JLabel("AVAILABLE BALANCE");
        balanceCaption.setFont(new Font("SansSerif", Font.BOLD, 12));
        balanceCaption.setForeground(UITheme.TEXT_MUTED);
        balanceCaption.setAlignmentX(Component.RIGHT_ALIGNMENT);

        balanceValueLabel = new JLabel("$0.00");
        balanceValueLabel.setFont(UITheme.FONT_BALANCE);
        balanceValueLabel.setForeground(UITheme.SUCCESS);
        balanceValueLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel liveStatus = new JLabel("● Instant Ledger Sync Active");
        liveStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        liveStatus.setForeground(UITheme.TEXT_MUTED);
        liveStatus.setAlignmentX(Component.RIGHT_ALIGNMENT);

        balancePanel.add(balanceCaption);
        balancePanel.add(Box.createVerticalStrut(6));
        balancePanel.add(balanceValueLabel);
        balancePanel.add(Box.createVerticalStrut(6));
        balancePanel.add(liveStatus);

        summaryCard.add(userInfoPanel, BorderLayout.WEST);
        summaryCard.add(balancePanel, BorderLayout.EAST);

        add(summaryCard, BorderLayout.NORTH);

        // Center Grid of 6 ATM Operations
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 18, 18));
        gridPanel.setOpaque(false);

        gridPanel.add(createMenuTile(
                "Cash Deposit",
                "Add funds with instant credit",
                "＋ DEPOSIT FUNDS",
                UITheme.SUCCESS,
                UITheme.SUCCESS_HOVER,
                () -> mainFrame.showScreen(ATMMainFrame.SCREEN_DEPOSIT)
        ));

        gridPanel.add(createMenuTile(
                "Cash Withdrawal",
                "Dispense cash up to $20,000",
                "－ WITHDRAW CASH",
                UITheme.PRIMARY,
                UITheme.PRIMARY_HOVER,
                () -> mainFrame.showScreen(ATMMainFrame.SCREEN_WITHDRAW)
        ));

        gridPanel.add(createMenuTile(
                "Fund Transfer",
                "Instant account-to-account transfer",
                "⇄ TRANSFER FUNDS",
                UITheme.PRIMARY,
                UITheme.PRIMARY_HOVER,
                () -> mainFrame.showScreen(ATMMainFrame.SCREEN_TRANSFER)
        ));

        gridPanel.add(createMenuTile(
                "Mini Statement",
                "View last 10 transactions & timestamps",
                "≡ VIEW STATEMENT",
                UITheme.SECONDARY,
                UITheme.SECONDARY_HOVER,
                () -> mainFrame.showScreen(ATMMainFrame.SCREEN_STATEMENT)
        ));

        gridPanel.add(createMenuTile(
                "Change ATM PIN",
                "Update your 4-digit security PIN",
                "🔒 CHANGE PIN",
                UITheme.SECONDARY,
                UITheme.SECONDARY_HOVER,
                () -> mainFrame.showScreen(ATMMainFrame.SCREEN_PIN_CHANGE)
        ));

        gridPanel.add(createMenuTile(
                "Eject Card / Exit",
                "End your ATM session securely",
                "⏻ LOGOUT SESSION",
                UITheme.DANGER,
                UITheme.DANGER_HOVER,
                this::handleLogout
        ));

        add(gridPanel, BorderLayout.CENTER);
    }

    private JPanel createMenuTile(String title, String description, String buttonLabel,
                                  Color btnColor, Color btnHover, Runnable action) {
        JPanel tile = UITheme.createCardPanel(new BorderLayout(0, 12));
        tile.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel textGroup = new JPanel();
        textGroup.setOpaque(false);
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_HEADING);
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel descLbl = new JLabel(description);
        descLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descLbl.setForeground(UITheme.TEXT_MUTED);

        textGroup.add(titleLbl);
        textGroup.add(Box.createVerticalStrut(6));
        textGroup.add(descLbl);

        JButton actionBtn = UITheme.createButton(buttonLabel, btnColor, btnHover);
        actionBtn.addActionListener(e -> action.run());

        tile.add(textGroup, BorderLayout.CENTER);
        tile.add(actionBtn, BorderLayout.SOUTH);
        return tile;
    }

    private void handleLogout() {
        controller.logout();
        mainFrame.showScreen(ATMMainFrame.SCREEN_LOGIN);
    }

    public void onShow() {
        controller.refreshCurrentUser();
        User user = controller.getCurrentUser();
        if (user == null) {
            mainFrame.showScreen(ATMMainFrame.SCREEN_LOGIN);
            return;
        }
        holderNameLabel.setText(user.getHolderName());
        accountMetaLabel.setText("Account Number:  #" + user.getAccountNumber());
        accountTypeBadge.setText("Account Type:  " + user.getAccountType().toUpperCase() + " ACCOUNT");
        balanceValueLabel.setText(String.format("$%,.2f", user.getBalance()));
    }
}
