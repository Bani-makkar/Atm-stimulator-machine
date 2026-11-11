# Horizon Bank: ATM Machine Simulator

A Java desktop ATM simulator built with **Swing, JDBC and an MVC/DAO architecture**. Log in with an account number and PIN, then deposit, withdraw, transfer money and review your transactions, with every money movement protected by a database transaction.

> Educational / portfolio project. It is not a production banking system.

<!-- Add screenshots here: ![Login](docs/login.png) ![Dashboard](docs/dashboard.png) -->

## Features

- Account number and 4-digit PIN login
- Dashboard with account details and balance
- Cash deposits and withdrawals with validation and limits
- Account-to-account transfers
- Mini statement of recent transactions
- PIN change with confirmation
- Pluggable database: SQLite by default, MySQL optionally, plus a built-in fallback so it runs with zero setup

## Tech stack

- Java 17+, Swing
- JDBC with prepared statements
- SQLite (default), MySQL (optional)
- Maven

## Design highlights

- **MVC + DAO:** Swing panels (`view`) talk to `ATMController`, which holds the business rules and uses `UserDAO` / `TransactionDAO` for data access.
- **Atomic operations:** deposits, withdrawals and transfers run inside JDBC transactions (`setAutoCommit(false)`, `commit`, `rollback`) so a failure never leaves balances half-updated.
- **Business rules:** per-transaction limits (withdrawal $20,000, deposit $100,000, transfer $50,000), minimum amount, and insufficient-funds checks.
- **PIN security:** new PINs are hashed with **PBKDF2-HMAC-SHA256** (120,000 iterations, random salt) and compared in constant time. Legacy SHA-256 / plaintext demo PINs still verify for old local databases.
- **SQL injection safe:** every query uses `PreparedStatement`.

## Getting started

### Prerequisites

- JDK 17 or newer
- Maven (optional, there are run scripts too)

### Run with Maven

```bash
mvn clean compile
mvn exec:java
```

### Run without Maven

```bash
./run.sh        # macOS / Linux
run.bat         # Windows
```

The tables and demo accounts are created on first launch.

### Demo accounts

| Account number | PIN | Name | Type | Opening balance |
|---|---:|---|---|---:|
| `1001001` | `1234` | Aarav Sharma | Savings | $25,000.00 |
| `1001002` | `4321` | Priya Patel | Current | $68,450.50 |
| `1001003` | `9999` | Rohan Verma | Savings | $12,300.00 |

These are sample credentials for local demonstration only.

## Database configuration

By default the app uses SQLite (`atm_simulator.db`). To use MySQL, set environment variables before running:

```bash
export ATM_DB_URL="jdbc:mysql://localhost:3306/atm_db"
export ATM_DB_USER="root"
export ATM_DB_PASS="your-local-password"
mvn exec:java
```

Never commit real passwords or database credentials.

## Project structure

```text
src/main/java/com/atm/
├── Main.java
├── controller/   ATMController: business rules and transactions
├── dao/          UserDAO, TransactionDAO, DatabaseConnection, EmbeddedJdbcDriver
├── model/        User, Transaction
├── util/         SecurityUtil (PIN hashing and validation)
└── view/         Swing panels and UI theme
sql/              Optional SQL schema
```

## Known limitations and future work

- Money is stored as `double`; a production system should use `BigDecimal` or integer cents
- No automated tests yet (JUnit tests for the controller and DAOs are the next step)
- Legacy PIN hashes are only upgraded when the user changes their PIN
- The embedded fallback store is for demos, not for concurrency or real persistence needs

## License

For learning and portfolio use.
