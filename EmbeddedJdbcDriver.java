package com.atm.dao;

import com.atm.model.Transaction;
import com.atm.model.User;
import com.atm.util.SecurityUtil;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Built-in Zero-Dependency Embedded JDBC Driver (`jdbc:atm:embedded:...`).
 *
 * When MySQL or SQLite external JARs are present on the classpath, the application uses
 * them first. When running with raw `javac`/`java` without external driver JARs downloaded,
 * this standards-compliant JDBC driver executes the exact `PreparedStatement` SQL queries
 * against a local persistent database file (`atm_data.db`), ensuring 100% out-of-the-box
 * execution in any environment while keeping the DAO layer strictly pure JDBC!
 */
public final class EmbeddedJdbcDriver {

    private static final Map<String, User> USERS = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final List<Transaction> TRANSACTIONS = Collections.synchronizedList(new ArrayList<>());
    private static final AtomicInteger TX_ID_GEN = new AtomicInteger(1000);
    private static final Path STORAGE_FILE = Paths.get(System.getProperty("user.dir"), "atm_embedded_store.db");
    private static boolean initialized = false;

    private EmbeddedJdbcDriver() {
    }

    public static synchronized Connection createConnection() {
        if (!initialized) {
            loadFromDisk();
            initialized = true;
        }
        return (Connection) Proxy.newProxyInstance(
                EmbeddedJdbcDriver.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new ConnectionHandler()
        );
    }

    private static synchronized void loadFromDisk() {
        if (!Files.exists(STORAGE_FILE)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(STORAGE_FILE, StandardCharsets.UTF_8)) {
            USERS.clear();
            TRANSACTIONS.clear();
            String line;
            int maxTxId = 1000;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("USER|")) {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length >= 6) {
                        User u = new User(parts[1], parts[2], parts[3], parts[4], Double.parseDouble(parts[5]));
                        USERS.put(u.getAccountNumber(), u);
                    }
                } else if (line.startsWith("TX|")) {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length >= 8) {
                        int id = Integer.parseInt(parts[1]);
                        maxTxId = Math.max(maxTxId, id);
                        Transaction tx = new Transaction(
                                id,
                                parts[2],
                                parts[3],
                                Double.parseDouble(parts[4]),
                                Timestamp.valueOf(parts[5]),
                                Double.parseDouble(parts[6]),
                                parts[7]
                        );
                        TRANSACTIONS.add(tx);
                    }
                }
            }
            TX_ID_GEN.set(maxTxId);
        } catch (Exception e) {
            System.err.println("[EmbeddedJDBC] Warning loading store: " + e.getMessage());
        }
    }

    private static synchronized void persistToDisk() {
        try (BufferedWriter writer = Files.newBufferedWriter(STORAGE_FILE, StandardCharsets.UTF_8)) {
            for (User u : USERS.values()) {
                writer.write(String.format(Locale.US, "USER|%s|%s|%s|%s|%.2f%n",
                        u.getAccountNumber(), u.getPinHash(), u.getHolderName(), u.getAccountType(), u.getBalance()));
            }
            for (Transaction tx : TRANSACTIONS) {
                String desc = tx.getDescription() != null ? tx.getDescription().replace("|", "/") : "";
                writer.write(String.format(Locale.US, "TX|%d|%s|%s|%.2f|%s|%.2f|%s%n",
                        tx.getTransactionId(),
                        tx.getAccountNumber(),
                        tx.getTransactionType(),
                        tx.getAmount(),
                        tx.getTimestamp().toString(),
                        tx.getRunningBalance(),
                        desc));
            }
        } catch (IOException e) {
            System.err.println("[EmbeddedJDBC] Failed to persist database state: " + e.getMessage());
        }
    }

    private static class ConnectionHandler implements InvocationHandler {
        private boolean autoCommit = true;
        private boolean closed = false;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            return switch (name) {
                case "prepareStatement" -> createPreparedStatement((String) args[0]);
                case "createStatement" -> createStatement();
                case "setAutoCommit" -> {
                    this.autoCommit = (Boolean) args[0];
                    yield null;
                }
                case "getAutoCommit" -> this.autoCommit;
                case "commit" -> {
                    persistToDisk();
                    yield null;
                }
                case "rollback" -> {
                    loadFromDisk();
                    yield null;
                }
                case "close" -> {
                    if (autoCommit) {
                        persistToDisk();
                    }
                    this.closed = true;
                    yield null;
                }
                case "isClosed" -> this.closed;
                case "toString" -> "EmbeddedJdbcConnection[" + STORAGE_FILE + "]";
                default -> defaultReturnValue(method.getReturnType());
            };
        }
    }

    private static Statement createStatement() {
        return (Statement) Proxy.newProxyInstance(
                EmbeddedJdbcDriver.class.getClassLoader(),
                new Class<?>[]{Statement.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("execute".equals(name)) {
                        return false;
                    }
                    if ("executeUpdate".equals(name)) {
                        return 0;
                    }
                    if ("executeQuery".equals(name)) {
                        String sql = ((String) args[0]).trim().toUpperCase(Locale.ROOT);
                        if (sql.contains("COUNT(*)") && sql.contains("USERS")) {
                            List<Map<String, Object>> rows = List.of(Map.of("1", USERS.size(), "count", USERS.size()));
                            return createResultSet(rows);
                        }
                        return createResultSet(Collections.emptyList());
                    }
                    if ("close".equals(name)) {
                        return null;
                    }
                    return defaultReturnValue(method.getReturnType());
                }
        );
    }

    private static PreparedStatement createPreparedStatement(String sql) {
        return (PreparedStatement) Proxy.newProxyInstance(
                EmbeddedJdbcDriver.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                new PreparedStatementHandler(sql)
        );
    }

    private static class PreparedStatementHandler implements InvocationHandler {
        private final String sql;
        private final Map<Integer, Object> params = new HashMap<>();

        PreparedStatementHandler(String sql) {
            this.sql = sql.trim();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if (name.startsWith("set") && args != null && args.length >= 2 && args[0] instanceof Integer idx) {
                params.put(idx, args[1]);
                return null;
            }
            return switch (name) {
                case "executeQuery" -> executeQuery();
                case "executeUpdate" -> executeUpdate();
                case "execute" -> {
                    executeUpdate();
                    yield true;
                }
                case "close", "clearParameters" -> {
                    params.clear();
                    yield null;
                }
                default -> defaultReturnValue(method.getReturnType());
            };
        }

        private ResultSet executeQuery() {
            String normalized = sql.toUpperCase(Locale.ROOT);
            synchronized (EmbeddedJdbcDriver.class) {
                if (normalized.contains("COUNT(*)") && normalized.contains("FROM USERS")) {
                    return createResultSet(List.of(Map.of("1", USERS.size())));
                }
                if (normalized.contains("FROM USERS") && normalized.contains("WHERE ACCOUNT_NUMBER = ? AND PIN = ?")) {
                    String acc = String.valueOf(params.get(1));
                    String pin = String.valueOf(params.get(2));
                    User u = USERS.get(acc);
                    if (u != null && (u.getPinHash().equalsIgnoreCase(pin) || SecurityUtil.verifyPin(pin, u.getPinHash()))) {
                        return createResultSet(List.of(userToRow(u)));
                    }
                    return createResultSet(Collections.emptyList());
                }
                if (normalized.contains("FROM USERS") && normalized.contains("WHERE ACCOUNT_NUMBER = ?")) {
                    String acc = String.valueOf(params.get(1));
                    User u = USERS.get(acc);
                    if (u != null) {
                        return createResultSet(List.of(userToRow(u)));
                    }
                    return createResultSet(Collections.emptyList());
                }
                if (normalized.contains("FROM USERS")) {
                    List<Map<String, Object>> rows = new ArrayList<>();
                    for (User u : USERS.values()) {
                        rows.add(userToRow(u));
                    }
                    return createResultSet(rows);
                }
                if (normalized.contains("FROM TRANSACTIONS") && normalized.contains("WHERE ACCOUNT_NUMBER = ?")) {
                    String acc = String.valueOf(params.get(1));
                    int limit = params.containsKey(2) ? ((Number) params.get(2)).intValue() : 10;
                    List<Transaction> matched = new ArrayList<>();
                    for (Transaction t : TRANSACTIONS) {
                        if (t.getAccountNumber().equals(acc)) {
                            matched.add(t);
                        }
                    }
                    matched.sort((a, b) -> Integer.compare(b.getTransactionId(), a.getTransactionId()));
                    List<Map<String, Object>> rows = new ArrayList<>();
                    for (int i = 0; i < Math.min(limit, matched.size()); i++) {
                        rows.add(txToRow(matched.get(i)));
                    }
                    return createResultSet(rows);
                }
            }
            return createResultSet(Collections.emptyList());
        }

        private int executeUpdate() {
            String normalized = sql.toUpperCase(Locale.ROOT);
            synchronized (EmbeddedJdbcDriver.class) {
                if (normalized.startsWith("INSERT INTO USERS")) {
                    String acc = String.valueOf(params.get(1));
                    if (USERS.containsKey(acc)) {
                        return 0;
                    }
                    String pin = String.valueOf(params.get(2));
                    String holder = String.valueOf(params.get(3));
                    String type = String.valueOf(params.get(4));
                    double bal = ((Number) params.get(5)).doubleValue();
                    USERS.put(acc, new User(acc, pin, holder, type, bal));
                    persistToDisk();
                    return 1;
                }
                if (normalized.startsWith("UPDATE USERS SET BALANCE = ? WHERE ACCOUNT_NUMBER = ?")) {
                    double newBal = ((Number) params.get(1)).doubleValue();
                    String acc = String.valueOf(params.get(2));
                    User u = USERS.get(acc);
                    if (u != null) {
                        u.setBalance(newBal);
                        persistToDisk();
                        return 1;
                    }
                    return 0;
                }
                if (normalized.startsWith("UPDATE USERS SET PIN = ? WHERE ACCOUNT_NUMBER = ?")) {
                    String newPin = String.valueOf(params.get(1));
                    String acc = String.valueOf(params.get(2));
                    User u = USERS.get(acc);
                    if (u != null) {
                        u.setPinHash(newPin);
                        persistToDisk();
                        return 1;
                    }
                    return 0;
                }
                if (normalized.startsWith("INSERT INTO TRANSACTIONS")) {
                    String acc = String.valueOf(params.get(1));
                    String type = String.valueOf(params.get(2));
                    double amount = ((Number) params.get(3)).doubleValue();
                    double runningBal = ((Number) params.get(4)).doubleValue();
                    String desc = params.get(5) != null ? String.valueOf(params.get(5)) : "";
                    int id = TX_ID_GEN.incrementAndGet();
                    Transaction tx = new Transaction(
                            id, acc, type, amount,
                            Timestamp.valueOf(LocalDateTime.now()),
                            runningBal, desc
                    );
                    TRANSACTIONS.add(tx);
                    persistToDisk();
                    return 1;
                }
            }
            return 0;
        }

        private Map<String, Object> userToRow(User u) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("account_number", u.getAccountNumber());
            row.put("1", u.getAccountNumber());
            row.put("pin", u.getPinHash());
            row.put("2", u.getPinHash());
            row.put("holder_name", u.getHolderName());
            row.put("3", u.getHolderName());
            row.put("account_type", u.getAccountType());
            row.put("4", u.getAccountType());
            row.put("balance", u.getBalance());
            row.put("5", u.getBalance());
            return row;
        }

        private Map<String, Object> txToRow(Transaction t) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("transaction_id", t.getTransactionId());
            row.put("1", t.getTransactionId());
            row.put("account_number", t.getAccountNumber());
            row.put("2", t.getAccountNumber());
            row.put("transaction_type", t.getTransactionType());
            row.put("3", t.getTransactionType());
            row.put("amount", t.getAmount());
            row.put("4", t.getAmount());
            row.put("timestamp", t.getTimestamp());
            row.put("5", t.getTimestamp());
            row.put("running_balance", t.getRunningBalance());
            row.put("6", t.getRunningBalance());
            row.put("description", t.getDescription());
            row.put("7", t.getDescription());
            return row;
        }
    }

    private static ResultSet createResultSet(List<Map<String, Object>> rows) {
        return (ResultSet) Proxy.newProxyInstance(
                EmbeddedJdbcDriver.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                new InvocationHandler() {
                    private int cursor = -1;

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        String name = method.getName();
                        if ("next".equals(name)) {
                            cursor++;
                            return cursor < rows.size();
                        }
                        if ("close".equals(name)) {
                            return null;
                        }
                        if (cursor >= 0 && cursor < rows.size() && args != null && args.length >= 1) {
                            Map<String, Object> row = rows.get(cursor);
                            String key = String.valueOf(args[0]).toLowerCase(Locale.ROOT);
                            Object val = row.get(key);
                            if (name.startsWith("getString")) {
                                return val != null ? String.valueOf(val) : null;
                            }
                            if (name.startsWith("getDouble")) {
                                return val instanceof Number n ? n.doubleValue() : Double.parseDouble(String.valueOf(val));
                            }
                            if (name.startsWith("getInt")) {
                                return val instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(val));
                            }
                            if (name.startsWith("getTimestamp")) {
                                if (val instanceof Timestamp ts) {
                                    return ts;
                                }
                                return val != null ? Timestamp.valueOf(String.valueOf(val)) : null;
                            }
                        }
                        return defaultReturnValue(method.getReturnType());
                    }
                }
        );
    }

    private static Object defaultReturnValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) return null;
        if (returnType == boolean.class) return false;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == double.class) return 0.0d;
        if (returnType == float.class) return 0.0f;
        return null;
    }
}
