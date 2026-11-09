-- =============================================================================
-- ATM Machine Simulator - Database Schema & Seed Data
-- Compatible with MySQL 8.0+ and SQLite 3+
-- =============================================================================

-- 1. Create Users Table
CREATE TABLE IF NOT EXISTS users (
    account_number VARCHAR(20) PRIMARY KEY,
    pin VARCHAR(128) NOT NULL,               -- SHA-256 hashed 4-digit PIN
    holder_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('Savings', 'Current')),
    balance DOUBLE NOT NULL DEFAULT 0.00 CHECK (balance >= 0.00)
);

-- 2. Create Transactions Table
-- Note: For MySQL use AUTO_INCREMENT; for SQLite use INTEGER PRIMARY KEY AUTOINCREMENT.
-- The Java DatabaseConnection initializer automatically adapts the DDL per dialect.
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    account_number VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,   -- 'Deposit', 'Withdrawal', 'Transfer Out', 'Transfer In'
    amount DOUBLE NOT NULL CHECK (amount > 0.00),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    running_balance DOUBLE NOT NULL,
    description VARCHAR(255),
    CONSTRAINT fk_account FOREIGN KEY (account_number)
        REFERENCES users(account_number)
        ON DELETE CASCADE
);

-- 3. Demo Seed Accounts
-- PIN '1234' -> SHA-256: 03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4
-- PIN '4321' -> SHA-256: fe2592b42a727e977f055947385b709cc82b16b9a87f88c6abf3900d65d0cdc3
-- PIN '9999' -> SHA-256: 888df25ae35772424a560c7152a1de794440e0ea5cfee62828333a456a506e05

INSERT INTO users (account_number, pin, holder_name, account_type, balance) VALUES
('1001001', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'Aarav Sharma', 'Savings', 25000.00),
('1001002', 'fe2592b42a727e977f055947385b709cc82b16b9a87f88c6abf3900d65d0cdc3', 'Priya Patel', 'Current', 68450.50),
('1001003', '888df25ae35772424a560c7152a1de794440e0ea5cfee62828333a456a506e05', 'Rohan Verma', 'Savings', 12300.00);
