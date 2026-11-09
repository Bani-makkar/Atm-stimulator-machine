package com.atm.dao;

import com.atm.model.User;
import com.atm.util.SecurityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for the `users` table.
 * Strictly uses {@link PreparedStatement} across all queries to prevent SQL injection.
 */
public class UserDAO {

    /**
     * Authenticates a user by account number and 4-digit PIN.
     *
     * @param accountNumber The user's account number
     * @param rawPin        The raw 4-digit PIN entered by the user
     * @return Optional containing the authenticated {@link User} if credentials match
     * @throws SQLException if a database error occurs
     */
    public Optional<User> authenticate(String accountNumber, String rawPin) throws SQLException {
        String sql = "SELECT account_number, pin, holder_name, account_type, balance FROM users WHERE account_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPinHash = rs.getString("pin");
                    if (SecurityUtil.verifyPin(rawPin, storedPinHash)) {
                        return Optional.of(mapRowToUser(rs));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a user by their account number.
     */
    public Optional<User> findByAccountNumber(String accountNumber) throws SQLException {
        String sql = "SELECT account_number, pin, holder_name, account_type, balance FROM users WHERE account_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a user using an existing transactional connection.
     */
    public Optional<User> findByAccountNumber(Connection conn, String accountNumber) throws SQLException {
        String sql = "SELECT account_number, pin, holder_name, account_type, balance FROM users WHERE account_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Updates the balance of a user inside an active transaction.
     */
    public boolean updateBalance(Connection conn, String accountNumber, double newBalance) throws SQLException {
        String sql = "UPDATE users SET balance = ? WHERE account_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, accountNumber.trim());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates the hashed PIN of an account after verifying the old PIN.
     */
    public boolean updatePin(String accountNumber, String newRawPin) throws SQLException {
        String sql = "UPDATE users SET pin = ? WHERE account_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, SecurityUtil.hashPin(newRawPin));
            ps.setString(2, accountNumber.trim());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lists all demo accounts (used for the quick-reference helper on the login screen).
     */
    public List<User> findAllUsers() throws SQLException {
        String sql = "SELECT account_number, pin, holder_name, account_type, balance FROM users";
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
        }
        return list;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("account_number"),
                rs.getString("pin"),
                rs.getString("holder_name"),
                rs.getString("account_type"),
                rs.getDouble("balance")
        );
    }
}
