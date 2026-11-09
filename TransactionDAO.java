package com.atm.dao;

import com.atm.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for the `transactions` table.
 * Strictly uses {@link PreparedStatement} across all queries to prevent SQL injection.
 */
public class TransactionDAO {

    /**
     * Records a new transaction inside an active database connection/transaction.
     */
    public void recordTransaction(Connection conn, String accountNumber, String transactionType,
                                  double amount, double runningBalance, String description) throws SQLException {
        String sql = "INSERT INTO transactions (account_number, transaction_type, amount, running_balance, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber.trim());
            ps.setString(2, transactionType);
            ps.setDouble(3, amount);
            ps.setDouble(4, runningBalance);
            ps.setString(5, description);
            ps.executeUpdate();
        }
    }

    /**
     * Fetches the most recent N transactions for the given account number.
     *
     * @param accountNumber The account number
     * @param limit         Maximum number of recent transactions (e.g., 10 for Mini Statement)
     * @return List of {@link Transaction} ordered newest first
     * @throws SQLException if a database error occurs
     */
    public List<Transaction> getRecentTransactions(String accountNumber, int limit) throws SQLException {
        String sql = """
                SELECT transaction_id, account_number, transaction_type, amount, timestamp, running_balance, description
                FROM transactions
                WHERE account_number = ?
                ORDER BY transaction_id DESC
                LIMIT ?
                """;
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber.trim());
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction(
                            rs.getInt("transaction_id"),
                            rs.getString("account_number"),
                            rs.getString("transaction_type"),
                            rs.getDouble("amount"),
                            rs.getTimestamp("timestamp"),
                            rs.getDouble("running_balance"),
                            rs.getString("description")
                    );
                    transactions.add(tx);
                }
            }
        }
        return transactions;
    }
}
