package com.atm.view;

import com.atm.controller.ATMController;
import com.atm.dao.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main Application JFrame hosting a {@link CardLayout} to transition smoothly
 * between all ATM Simulator screens:
 * - Login Screen
 * - Main Dashboard
 * - Cash Deposit Screen
 * - Cash Withdrawal Screen
 * - Fund Transfer Screen
 * - Mini Statement / History Screen
 * - PIN Change Screen
 */
public class ATMMainFrame extends JFrame {

    public static final String SCREEN_LOGIN = "LOGIN";
    public static final String SCREEN_DASHBOARD = "DASHBOARD";
    public static final String SCREEN_DEPOSIT = "DEPOSIT";
    public static final String SCREEN_WITHDRAW = "WITHDRAW";
    public static final String SCREEN_TRANSFER = "TRANSFER";
    public static final String SCREEN_STATEMENT = "STATEMENT";
    public static final String SCREEN_PIN_CHANGE = "PIN_CHANGE";

    private final ATMController controller;
    private final CardLayout cardLayout;
    private final JPanel cardContainer;

    private final LoginPanel loginPanel;
    private final DashboardPanel dashboardPanel;
    private final DepositPanel depositPanel;
    private final WithdrawalPanel withdrawalPanel;
    private final TransferPanel transferPanel;
    private final MiniStatementPanel miniStatementPanel;
    private final PinChangePanel pinChangePanel;

    private final JLabel statusEngineLabel;

    public ATMMainFrame(ATMController controller) {
        super("Horizon Bank — Advanced ATM Simulator");
        this.controller = controller;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(940, 680);
        setMinimumSize(new Dimension(860, 620));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);

        // Top ATM Header Banner
        root.add(createHeaderBar(), BorderLayout.NORTH);

        // Center Card Container
        this.cardLayout = new CardLayout();
        this.cardContainer = new JPanel(cardLayout);
        this.cardContainer.setBackground(UITheme.BG_DARK);
        this.cardContainer.setBorder(new EmptyBorder(20, 28, 20, 28));

        // Instantiate all MVC View screens
        this.loginPanel = new LoginPanel(controller, this);
        this.dashboardPanel = new DashboardPanel(controller, this);
        this.depositPanel = new DepositPanel(controller, this);
        this.withdrawalPanel = new WithdrawalPanel(controller, this);
        this.transferPanel = new TransferPanel(controller, this);
        this.miniStatementPanel = new MiniStatementPanel(controller, this);
        this.pinChangePanel = new PinChangePanel(controller, this);

        cardContainer.add(loginPanel, SCREEN_LOGIN);
        cardContainer.add(dashboardPanel, SCREEN_DASHBOARD);
        cardContainer.add(depositPanel, SCREEN_DEPOSIT);
        cardContainer.add(withdrawalPanel, SCREEN_WITHDRAW);
        cardContainer.add(transferPanel, SCREEN_TRANSFER);
        cardContainer.add(miniStatementPanel, SCREEN_STATEMENT);
        cardContainer.add(pinChangePanel, SCREEN_PIN_CHANGE);

        root.add(cardContainer, BorderLayout.CENTER);

        // Bottom Status Bar
        this.statusEngineLabel = new JLabel();
        root.add(createStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);
        showScreen(SCREEN_LOGIN);
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(10, 15, 29));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_SUBTLE),
                new EmptyBorder(14, 28, 14, 28)
        ));

        JLabel title = new JLabel("HORIZON BANK  |  24x7 EXPRESS ATM TERMINAL");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(UITheme.ACCENT_CYAN);

        JLabel badge = new JLabel("SSL/SHA-256 SECURED  •  TERMINAL #ATM-094");
        badge.setFont(new Font("SansSerif", Font.PLAIN, 12));
        badge.setForeground(UITheme.TEXT_MUTED);

        header.add(title, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    private JPanel createStatusBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(10, 15, 29));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_SUBTLE),
                new EmptyBorder(8, 28, 8, 28)
        ));

        statusEngineLabel.setText("Database Engine: " + DatabaseConnection.getActiveEngineDescription());
        statusEngineLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusEngineLabel.setForeground(UITheme.TEXT_MUTED);

        JLabel helpLabel = new JLabel("Demo Accounts:  1001001 (PIN: 1234)   |   1001002 (PIN: 4321)   |   1001003 (PIN: 9999)");
        helpLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        helpLabel.setForeground(UITheme.ACCENT_CYAN);

        footer.add(statusEngineLabel, BorderLayout.WEST);
        footer.add(helpLabel, BorderLayout.EAST);
        return footer;
    }

    /**
     * Switches to the specified screen and refreshes its dynamic state.
     */
    public void showScreen(String screenName) {
        statusEngineLabel.setText("Database Engine: " + DatabaseConnection.getActiveEngineDescription());
        switch (screenName) {
            case SCREEN_LOGIN -> loginPanel.onShow();
            case SCREEN_DASHBOARD -> dashboardPanel.onShow();
            case SCREEN_DEPOSIT -> depositPanel.onShow();
            case SCREEN_WITHDRAW -> withdrawalPanel.onShow();
            case SCREEN_TRANSFER -> transferPanel.onShow();
            case SCREEN_STATEMENT -> miniStatementPanel.onShow();
            case SCREEN_PIN_CHANGE -> pinChangePanel.onShow();
        }
        cardLayout.show(cardContainer, screenName);
    }
}
