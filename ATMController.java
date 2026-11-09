package com.atm.controller;

import com.atm.dao.DatabaseConnection;
import com.atm.dao.TransactionDAO;
import com.atm.dao.UserDAO;
import com.atm.model.Transaction;
import com.atm.model.User;
import com.atm.util.SecurityUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controller layer in the MVC architecture.
 * Encapsulates ATM business rules, session state, validation limits, and atomic
 * database transactions across {@link UserDAO} and {@link TransactionDAO}.
 */
public class ATMController {

    public static final double MAX_SINGLE_WITHDRAWAL_LIMIT = 20000.00;
    public static final double MAX_SINGLE_DEPOSIT_LIMIT = 100000.00;
    public static final double MAX_TRANSFER_LIMIT = 50000.00;
    public static final double MIN_TRANSACTION_AMOUNT = 1.00;

    private final UserDAO userDAO;
    private final TransactionDAO transactionDAO;
    private User currentUser;

    public ATMController() {
        this.userDAO = new UserDAO();
        this.transactionDAO = new TransactionDAO();
    }

    /**
     * Result wrapper for ATM operations providing structured feedback to the View.
     */
    public record OperationResult(boolean success, String message, double updatedBalance) {
        public static OperationResult ok(String message, double balance) {
            return new OperationResult(true, message, balance);
        }

        public static OperationResult fail(String message) {
            return new OperationResult(false, message, 0.0);
        }
    }

    /**
     * Authenticates a user by Account Number and 4-digit PIN.
     */
    public OperationResult login(String accountNumber, String pin) {
        if (!SecurityUtil.isValidAccountNumber(accountNumber)) {
            return OperationResult.fail("Invalid Account Number. Please enter a 5 to 16 digit account number.");
        }
        if (!SecurityUtil.isValidPinFormat(pin)) {
            return OperationResult.fail("Invalid PIN format. PIN must be exactly 4 numeric digits.");
        }

        try {
            Optional<User> userOpt = userDAO.authenticate(accountNumber, pin);
            if (userOpt.isPresent()) {
                this.currentUser = userOpt.get();
                return OperationResult.ok("Welcome, " + currentUser.getHolderName() + "!", currentUser.getBalance());
            } else {
                return OperationResult.fail("Authentication failed. Invalid Account Number or PIN.");
            }
        } catch (SQLException e) {
            return OperationResult.fail("Database error during login: " + e.getMessage());
        }
    }

    /**
     * Ends the current ATM session.
     */
    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Refreshes the active user's record from the database.
     */
    public void refreshCurrentUser() {
        if (currentUser == null) return;
        try {
            userDAO.findByAccountNumber(currentUser.getAccountNumber())
                    .ifPresent(u -> this.currentUser = u);
        } catch (SQLException ignored) {
        }
    }

    /**
     * Processes a cash deposit with validation and atomic database commit.
     */
    public OperationResult deposit(double amount) {
        if (currentUser == null) {
            return OperationResult.fail("No active session. Please log in again.");
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < MIN_TRANSACTION_AMOUNT) {
            return OperationResult.fail(String.format("Deposit amount must be at least $%.2f.", MIN_TRANSACTION_AMOUNT));
        }
        if (amount > MAX_SINGLE_DEPOSIT_LIMIT) {
            return OperationResult.fail(String.format("Maximum single deposit limit is $%,.2f.", MAX_SINGLE_DEPOSIT_LIMIT));
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                User freshUser = userDAO.findByAccountNumber(conn, currentUser.getAccountNumber())
                        .orElseThrow(() -> new SQLException("Account not found."));

                double newBalance = roundCurrency(freshUser.getBalance() + amount);
                userDAO.updateBalance(conn, freshUser.getAccountNumber(), newBalance);
                transactionDAO.recordTransaction(
                        conn,
                        freshUser.getAccountNumber(),
                        "Deposit",
                        amount,
                        newBalance,
                        "ATM Cash Deposit"
                );

                conn.commit();
                currentUser.setBalance(newBalance);
                return OperationResult.ok(
                        String.format("Successfully deposited $%,.2f. Updated Balance: $%,.2f", amount, newBalance),
                        newBalance
                );
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return OperationResult.fail("Transaction failed due to database error: " + e.getMessage());
        }
    }

    /**
     * Processes a cash withdrawal with insufficient-funds and ATM limit checks.
     */
    public OperationResult withdraw(double amount) {
        if (currentUser == null) {
            return OperationResult.fail("No active session. Please log in again.");
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < MIN_TRANSACTION_AMOUNT) {
            return OperationResult.fail(String.format("Withdrawal amount must be at least $%.2f.", MIN_TRANSACTION_AMOUNT));
        }
        if (amount > MAX_SINGLE_WITHDRAWAL_LIMIT) {
            return OperationResult.fail(String.format("ATM single withdrawal limit exceeded (Max: $%,.2f).", MAX_SINGLE_WITHDRAWAL_LIMIT));
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                User freshUser = userDAO.findByAccountNumber(conn, currentUser.getAccountNumber())
                        .orElseThrow(() -> new SQLException("Account not found."));

                if (freshUser.getBalance() < amount) {
                    conn.rollback();
                    return OperationResult.fail(String.format(
                            "Insufficient funds. Your available balance is $%,.2f.", freshUser.getBalance()));
                }

                double newBalance = roundCurrency(freshUser.getBalance() - amount);
                userDAO.updateBalance(conn, freshUser.getAccountNumber(), newBalance);
                transactionDAO.recordTransaction(
                        conn,
                        freshUser.getAccountNumber(),
                        "Withdrawal",
                        amount,
                        newBalance,
                        "ATM Cash Withdrawal"
                );

                conn.commit();
                currentUser.setBalance(newBalance);
                return OperationResult.ok(
                        String.format("Please collect your cash ($%,.2f). Remaining Balance: $%,.2f", amount, newBalance),
                        newBalance
                );
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return OperationResult.fail("Withdrawal failed due to database error: " + e.getMessage());
        }
    }

    /**
     * Atomically transfers funds between the current user's account and a recipient account.
     */
    public OperationResult transferFunds(String targetAccountNumber, double amount) {
        if (currentUser == null) {
            return OperationResult.fail("No active session. Please log in again.");
        }
        if (!SecurityUtil.isValidAccountNumber(targetAccountNumber)) {
            return OperationResult.fail("Please enter a valid recipient account number (5-16 digits).");
        }
        String trimmedTarget = targetAccountNumber.trim();
        if (trimmedTarget.equals(currentUser.getAccountNumber())) {
            return OperationResult.fail("Cannot transfer funds to the same source account.");
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < MIN_TRANSACTION_AMOUNT) {
            return OperationResult.fail(String.format("Transfer amount must be at least $%.2f.", MIN_TRANSACTION_AMOUNT));
        }
        if (amount > MAX_TRANSFER_LIMIT) {
            return OperationResult.fail(String.format("Transfer exceeds maximum per-transaction limit ($%,.2f).", MAX_TRANSFER_LIMIT));
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                User sender = userDAO.findByAccountNumber(conn, currentUser.getAccountNumber())
                        .orElseThrow(() -> new SQLException("Source account not found."));

                Optional<User> recipientOpt = userDAO.findByAccountNumber(conn, trimmedTarget);
                if (recipientOpt.isEmpty()) {
                    conn.rollback();
                    return OperationResult.fail("Recipient account (" + trimmedTarget + ") does not exist.");
                }
                User recipient = recipientOpt.get();

                if (sender.getBalance() < amount) {
                    conn.rollback();
                    return OperationResult.fail(String.format(
                            "Insufficient funds for transfer. Available balance: $%,.2f", sender.getBalance()));
                }

                double newSenderBalance = roundCurrency(sender.getBalance() - amount);
                double newRecipientBalance = roundCurrency(recipient.getBalance() + amount);

                userDAO.updateBalance(conn, sender.getAccountNumber(), newSenderBalance);
                userDAO.updateBalance(conn, recipient.getAccountNumber(), newRecipientBalance);

                transactionDAO.recordTransaction(
                        conn,
                        sender.getAccountNumber(),
                        "Transfer Out",
                        amount,
                        newSenderBalance,
                        "Transfer to " + recipient.getHolderName() + " (#" + recipient.getAccountNumber() + ")"
                );

                transactionDAO.recordTransaction(
                        conn,
                        recipient.getAccountNumber(),
                        "Transfer In",
                        amount,
                        newRecipientBalance,
                        "Transfer from " + sender.getHolderName() + " (#" + sender.getAccountNumber() + ")"
                );

                conn.commit();
                currentUser.setBalance(newSenderBalance);
                return OperationResult.ok(
                        String.format("Transferred $%,.2f to %s (#%s). Remaining Balance: $%,.2f",
                                amount, recipient.getHolderName(), recipient.getAccountNumber(), newSenderBalance),
                        newSenderBalance
                );
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return OperationResult.fail("Fund transfer failed: " + e.getMessage());
        }
    }

    /**
     * Verifies the user's current PIN, validates the new PIN & confirmation, and updates the hashed PIN.
     */
    public OperationResult changePin(String oldPin, String newPin, String confirmPin) {
        if (currentUser == null) {
            return OperationResult.fail("No active session. Please log in again.");
        }
        if (!SecurityUtil.isValidPinFormat(oldPin)) {
            return OperationResult.fail("Current PIN must be 4 numeric digits.");
        }
        if (!SecurityUtil.isValidPinFormat(newPin)) {
            return OperationResult.fail("New PIN must be exactly 4 numeric digits.");
        }
        if (!newPin.equals(confirmPin)) {
            return OperationResult.fail("New PIN and Confirmation PIN do not match.");
        }
        if (oldPin.equals(newPin)) {
            return OperationResult.fail("New PIN must be different from your current PIN.");
        }

        try {
            Optional<User> verified = userDAO.authenticate(currentUser.getAccountNumber(), oldPin);
            if (verified.isEmpty()) {
                return OperationResult.fail("Current PIN verification failed. Incorrect old PIN.");
            }

            boolean updated = userDAO.updatePin(currentUser.getAccountNumber(), newPin);
            if (updated) {
                currentUser.setPinHash(SecurityUtil.hashPin(newPin));
                return OperationResult.ok("Your 4-digit ATM PIN has been updated securely.", currentUser.getBalance());
            } else {
                return OperationResult.fail("Could not update PIN. Please try again.");
            }
        } catch (SQLException e) {
            return OperationResult.fail("Database error while updating PIN: " + e.getMessage());
        }
    }

    /**
     * Retrieves the last N transactions for the logged-in user's Mini Statement.
     */
    public List<Transaction> getMiniStatement(int limit) {
        if (currentUser == null) {
            return Collections.emptyList();
        }
        try {
            return transactionDAO.getRecentTransactions(currentUser.getAccountNumber(), limit);
        } catch (SQLException e) {
            System.err.println("[ATMController] Failed to load mini statement: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private static double roundCurrency(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
