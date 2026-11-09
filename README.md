# Horizon Bank — ATM Machine Simulator

A complete, production-ready desktop **ATM Machine Simulator** built with **Java (JDK 17+)**, **Swing/AWT**, **JDBC**, and a clean **MVC + DAO architecture**.

---

## Features

- **Login Screen**: Account Number & 4-digit PIN authentication (SHA-256 hashed), interactive ATM PIN keypad, and 1-click Demo Account quick-fill buttons.
- **Main Dashboard**: Displays authenticated account holder name, account number, account type (`Savings` / `Current`), real-time formatted balance, and 6 ATM operation tiles.
- **Cash Deposit Screen**: Custom deposit input + quick denomination buttons (`+$50`, `+$100`, `+$500`, `+$1000`, `+$5000`) with instant atomic DB commit.
- **Cash Withdrawal Screen**: Custom withdrawal input + Fast Cash buttons with insufficient-funds and ATM per-transaction limit validation (`$20,000`).
- **Fund Transfer Screen**: Atomic account-to-account transfer between source and recipient accounts with balance & recipient verification.
- **Mini Statement / History Screen**: Styled `JTable` displaying the last 10 transactions with timestamps, amounts, and running balances + printable receipt view.
- **PIN Change Screen**: Verifies current 4-digit PIN and updates the new SHA-256 hashed PIN after confirmation match.
- **Multi-Engine JDBC Support**: Works with **MySQL**, **SQLite**, and includes a built-in zero-dependency **Embedded JDBC Driver** (`EmbeddedJdbcDriver`) for instant execution out of the box.

---

## Project Structure

```text
atm-simulator/
├── pom.xml
├── run.sh
├── sql/
│   └── schema.sql
└── src/main/java/com/atm/
    ├── Main.java
    ├── model/
    │   ├── User.java
    │   └── Transaction.java
    ├── util/
    │   └── SecurityUtil.java
    ├── dao/
    │   ├── DatabaseConnection.java
    │   ├── EmbeddedJdbcDriver.java
    │   ├── UserDAO.java
    │   └── TransactionDAO.java
    ├── controller/
    │   └── ATMController.java
    └── view/
        ├── UITheme.java
        ├── ATMMainFrame.java
        ├── LoginPanel.java
        ├── DashboardPanel.java
        ├── DepositPanel.java
        ├── WithdrawalPanel.java
        ├── TransferPanel.java
        ├── MiniStatementPanel.java
        └── PinChangePanel.java
```

---

## Quick Start

```zsh
chmod +x run.sh
./run.sh
```

### Demo Accounts

| Account Number | 4-Digit PIN | Holder Name | Account Type | Opening Balance |
| :--- | :--- | :--- | :--- | :--- |
| `1001001` | `1234` | Aarav Sharma | Savings | `$25,000.00` |
| `1001002` | `4321` | Priya Patel | Current | `$68,450.50` |
| `1001003` | `9999` | Rohan Verma | Savings | `$12,300.00` |

### Run Automated Self-Test

```zsh
./run.sh --self-test
```
