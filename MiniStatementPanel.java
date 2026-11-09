package com.atm.view;

import com.atm.controller.ATMController;
import com.atm.model.Transaction;
import com.atm.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Mini Statement / Transaction History Screen:
 * - Displays the last 10 transactions with timestamps, transaction types, amounts,
 *   running balance, and remarks in a custom-styled {@link JTable}.
 * - Also provides a Printable Receipt preview modal.
 */
public class MiniStatementPanel extends JPanel {

    private static final int STATEMENT_LIMIT = 10;

    private final ATMController controller;
    private final ATMMainFrame mainFrame;

    private final JLabel accountHeaderLabel;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public MiniStatementPanel(ATMController controller, ATMMainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 18));
        setBackground(UITheme.BG_DARK);

        JPanel card = UITheme.createCardPanel(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Mini Statement — Recent 10 Transactions");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        accountHeaderLabel = new JLabel("Account #—  |  Balance: $0.00");
        accountHeaderLabel.setFont(UITheme.FONT_BOLD);
        accountHeaderLabel.setForeground(UITheme.ACCENT_CYAN);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(accountHeaderLabel, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"TX ID", "TIMESTAMP", "TYPE", "AMOUNT ($)", "RUNNING BALANCE ($)", "DESCRIPTION"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setBackground(UITheme.BG_INPUT);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setFont(new Font("Monospaced", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setGridColor(UITheme.BORDER_SUBTLE);
        table.setSelectionBackground(UITheme.PRIMARY.darker());
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(15, 23, 42));
        header.setForeground(UITheme.ACCENT_CYAN);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        // Custom cell alignment and color coding for Deposit / Withdrawal / Transfer
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UITheme.BG_INPUT : new Color(22, 32, 52));
                    String type = String.valueOf(tbl.getValueAt(row, 2));
                    if (col == 2 || col == 3) {
                        if (type.contains("Deposit") || type.contains("Transfer In")) {
                            c.setForeground(UITheme.SUCCESS);
                        } else {
                            c.setForeground(new Color(248, 113, 113));
                        }
                    } else {
                        c.setForeground(UITheme.TEXT_PRIMARY);
                    }
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(155);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(220);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_SUBTLE, 1));
        scrollPane.getViewport().setBackground(UITheme.BG_INPUT);
        card.add(scrollPane, BorderLayout.CENTER);

        // Bottom Buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);

        JButton printReceiptBtn = UITheme.createButton("🖨 VIEW RECEIPT TEXT", UITheme.SECONDARY, UITheme.SECONDARY_HOVER);
        printReceiptBtn.addActionListener(e -> showPrintableReceiptModal());

        JButton refreshBtn = UITheme.createButton("↻ REFRESH", UITheme.PRIMARY, UITheme.PRIMARY_HOVER);
        refreshBtn.addActionListener(e -> onShow());

        JButton backBtn = UITheme.createButton("← BACK TO DASHBOARD", UITheme.SECONDARY, UITheme.SECONDARY_HOVER);
        backBtn.addActionListener(e -> mainFrame.showScreen(ATMMainFrame.SCREEN_DASHBOARD));

        footer.add(backBtn);
        footer.add(refreshBtn);
        footer.add(printReceiptBtn);

        card.add(footer, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);
    }

    private void showPrintableReceiptModal() {
        User user = controller.getCurrentUser();
        if (user == null) return;
        List<Transaction> txList = controller.getMiniStatement(STATEMENT_LIMIT);

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================================\n");
        sb.append("              HORIZON BANK - ATM MINI STATEMENT           \n");
        sb.append("==========================================================\n");
        sb.append(String.format("Account Holder : %s%n", user.getHolderName()));
        sb.append(String.format("Account Number : #%s (%s)%n", user.getAccountNumber(), user.getAccountType()));
        sb.append(String.format("Current Balance: $%,.2f%n", user.getBalance()));
        sb.append("----------------------------------------------------------\n");
        sb.append(String.format("%-19s | %-12s | %10s | %10s%n", "TIMESTAMP", "TYPE", "AMOUNT", "BALANCE"));
        sb.append("----------------------------------------------------------\n");
        for (Transaction tx : txList) {
            String sign = (tx.getTransactionType().contains("Deposit") || tx.getTransactionType().contains("In")) ? "+" : "-";
            sb.append(String.format("%-19s | %-12s | %s$%8.2f | $%9.2f%n",
                    tx.getFormattedTimestamp(),
                    tx.getTransactionType(),
                    sign,
                    tx.getAmount(),
                    tx.getRunningBalance()));
        }
        sb.append("==========================================================\n");

        JTextArea receiptArea = new JTextArea(sb.toString(), 18, 58);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setEditable(false);
        receiptArea.setBackground(UITheme.BG_DARK);
        receiptArea.setForeground(UITheme.TEXT_PRIMARY);
        receiptArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JOptionPane.showMessageDialog(this, new JScrollPane(receiptArea),
                "ATM Printable Mini Statement", JOptionPane.PLAIN_MESSAGE);
    }

    public void onShow() {
        controller.refreshCurrentUser();
        User user = controller.getCurrentUser();
        if (user == null) {
            return;
        }
        accountHeaderLabel.setText(String.format("Account #%s (%s)  |  Balance: $%,.2f",
                user.getAccountNumber(), user.getAccountType(), user.getBalance()));

        tableModel.setRowCount(0);
        List<Transaction> transactions = controller.getMiniStatement(STATEMENT_LIMIT);
        for (Transaction tx : transactions) {
            String prefix = (tx.getTransactionType().contains("Deposit") || tx.getTransactionType().contains("In"))
                    ? "+$" : "-$";
            tableModel.addRow(new Object[]{
                    "#" + tx.getTransactionId(),
                    tx.getFormattedTimestamp(),
                    tx.getTransactionType(),
                    String.format("%s%,.2f", prefix, tx.getAmount()),
                    String.format("$%,.2f", tx.getRunningBalance()),
                    tx.getDescription() != null ? tx.getDescription() : ""
            });
        }
    }
}
