package com.atm.model;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/**
 * Model representing an individual financial transaction.
 * Maps directly to the `transactions` table in the database.
 */
public class Transaction {
    private int transactionId;
    private String accountNumber;
    private String transactionType; // "Deposit", "Withdrawal", "Transfer Out", "Transfer In"
    private double amount;
    private Timestamp timestamp;
    private double runningBalance;
    private String description;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Transaction() {
    }

    public Transaction(int transactionId, String accountNumber, String transactionType,
                       double amount, Timestamp timestamp, double runningBalance, String description) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.timestamp = timestamp;
        this.runningBalance = runningBalance;
        this.description = description;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTimestamp() {
        if (timestamp == null) {
            return "";
        }
        return timestamp.toLocalDateTime().format(FORMATTER);
    }

    public double getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(double runningBalance) {
        this.runningBalance = runningBalance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
