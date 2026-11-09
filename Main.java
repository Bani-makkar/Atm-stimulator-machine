package com.atm;

import com.atm.controller.ATMController;
import com.atm.controller.ATMController.OperationResult;
import com.atm.dao.DatabaseConnection;
import com.atm.model.Transaction;
import com.atm.view.ATMMainFrame;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Application entry point for the ATM Machine Simulator.
 * Initializes the database schema & demo accounts, then launches the Java Swing
 * MVC Desktop Interface on the Event Dispatch Thread (EDT).
 *
 * Also supports `--self-test` CLI flag for automated end-to-end verification of all
 * JDBC / Controller operations.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Initialize database schema & seed demo accounts if needed
        DatabaseConnection.initializeDatabase();

        // Optional automated verification mode (`java com.atm.Main --self-test`)
        if (args.length > 0 && "--self-test".equalsIgnoreCase(args[0])) {
            runAutomatedSelfTest();
            return;
        }

        // 2. Configure Swing Look & Feel and launch GUI on Event Dispatch Thread
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            if (GraphicsEnvironment.isHeadless()) {
                System.out.println("Headless environment detected. Running self-test instead:");
                runAutomatedSelfTest();
                return;
            }

            ATMController controller = new ATMController();
            ATMMainFrame mainFrame = new ATMMainFrame(controller);
            mainFrame.setVisible(true);
        });
    }

    private static void runAutomatedSelfTest() {
        System.out.println("=== ATM Machine Simulator — Automated End-to-End Verification ===");
        System.out.println("Active JDBC Engine: " + DatabaseConnection.getActiveEngineDescription());

        ATMController controller = new ATMController();

        // 1. Test Login
        OperationResult loginRes = controller.login("1001001", "1234");
        assertCondition(loginRes.success(), "Login with valid credentials (1001001 / 1234): " + loginRes.message());

        OperationResult badLogin = controller.login("1001001", "0000");
        assertCondition(!badLogin.success(), "Reject invalid PIN (0000): " + badLogin.message());

        // Re-login
        controller.login("1001001", "1234");
        double startBal = controller.getCurrentUser().getBalance();

        // 2. Test Deposit
        OperationResult depRes = controller.deposit(1500.00);
        assertCondition(depRes.success() && Math.abs(depRes.updatedBalance() - (startBal + 1500.00)) < 0.01,
                "Cash Deposit ($1,500.00): " + depRes.message());

        // 3. Test Withdrawal
        OperationResult witRes = controller.withdraw(500.00);
        assertCondition(witRes.success() && Math.abs(witRes.updatedBalance() - (startBal + 1000.00)) < 0.01,
                "Cash Withdrawal ($500.00): " + witRes.message());

        // 4. Test Insufficient Funds Withdrawal
        OperationResult overWithdraw = controller.withdraw(999999.00);
        assertCondition(!overWithdraw.success(), "Reject excessive withdrawal: " + overWithdraw.message());

        // 5. Test Fund Transfer to 1001002
        OperationResult tfRes = controller.transferFunds("1001002", 1000.00);
        assertCondition(tfRes.success() && Math.abs(tfRes.updatedBalance() - startBal) < 0.01,
                "Fund Transfer ($1,000.00 -> 1001002): " + tfRes.message());

        // 6. Test Mini Statement
        List<Transaction> statement = controller.getMiniStatement(10);
        assertCondition(statement.size() >= 3,
                "Mini Statement retrieved " + statement.size() + " recent transactions.");

        // 7. Test PIN Change & Restore
        OperationResult pinChange = controller.changePin("1234", "5678", "5678");
        assertCondition(pinChange.success(), "PIN Change (1234 -> 5678): " + pinChange.message());

        OperationResult restorePin = controller.changePin("5678", "1234", "1234");
        assertCondition(restorePin.success(), "PIN Restore (5678 -> 1234): " + restorePin.message());

        System.out.println("=== ALL ATM MVC + JDBC SELF-TESTS PASSED SUCCESSFULLY ===");
    }

    private static void assertCondition(boolean condition, String detail) {
        if (!condition) {
            throw new IllegalStateException("TEST FAILED: " + detail);
        }
        System.out.println("  [PASS] " + detail);
    }
}
