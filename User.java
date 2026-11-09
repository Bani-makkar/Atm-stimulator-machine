package com.atm.model;

/**
 * Model representing a bank account user in the ATM system.
 * Maps directly to the `users` table in the database.
 */
public class User {
    private String accountNumber;
    private String pinHash;
    private String holderName;
    private String accountType; // "Savings" or "Current"
    private double balance;

    public User() {
    }

    public User(String accountNumber, String pinHash, String holderName, String accountType, double balance) {
        this.accountNumber = accountNumber;
        this.pinHash = pinHash;
        this.holderName = holderName;
        this.accountType = accountType;
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return String.format("User[account=%s, holder=%s, type=%s, balance=%.2f]",
                accountNumber, holderName, accountType, balance);
    }
}
