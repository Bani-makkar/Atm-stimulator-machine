package com.atm.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Design system and reusable custom Swing component factories for a modern dark/slate
 * FinTech ATM interface.
 */
public final class UITheme {

    // Palette
    public static final Color BG_DARK = new Color(15, 23, 42);         // Slate 900
    public static final Color BG_CARD = new Color(30, 41, 59);         // Slate 800
    public static final Color BG_INPUT = new Color(15, 23, 42);        // Slate 900
    public static final Color BORDER_SUBTLE = new Color(51, 65, 85);   // Slate 700

    public static final Color PRIMARY = new Color(37, 99, 235);        // Blue 600
    public static final Color PRIMARY_HOVER = new Color(59, 130, 246); // Blue 500
    public static final Color SUCCESS = new Color(16, 185, 129);       // Emerald 500
    public static final Color SUCCESS_HOVER = new Color(5, 150, 105);  // Emerald 600
    public static final Color WARNING = new Color(245, 158, 11);       // Amber 500
    public static final Color DANGER = new Color(239, 68, 68);         // Red 500
    public static final Color DANGER_HOVER = new Color(220, 38, 38);   // Red 600
    public static final Color SECONDARY = new Color(71, 85, 105);      // Slate 600
    public static final Color SECONDARY_HOVER = new Color(100, 116, 139);

    public static final Color TEXT_PRIMARY = new Color(248, 250, 252); // Slate 50
    public static final Color TEXT_MUTED = new Color(148, 163, 184);   // Slate 400
    public static final Color ACCENT_CYAN = new Color(56, 189, 248);   // Sky 400

    // Fonts
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_MONO = new Font("Monospaced", Font.BOLD, 16);
    public static final Font FONT_BALANCE = new Font("SansSerif", Font.BOLD, 30);

    private UITheme() {
    }

    /**
     * Creates a styled rounded button with hover effects.
     */
    public static JButton createButton(String text, Color baseColor, Color hoverColor) {
        JButton button = new JButton(text) {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? (hovered ? hoverColor : baseColor) : SECONDARY.darker());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BOLD);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(12, 20, 12, 20));
        return button;
    }

    /**
     * Creates a styled text input field.
     */
    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        styleInputComponent(field);
        return field;
    }

    /**
     * Creates a styled password/PIN input field.
     */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        styleInputComponent(field);
        return field;
    }

    private static void styleInputComponent(JTextField field) {
        field.setFont(FONT_MONO);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_INPUT);
        field.setCaretColor(ACCENT_CYAN);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
    }

    /**
     * Creates a rounded card panel.
     */
    public static JPanel createCardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(BORDER_SUBTLE);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Shows a consistent dialog alert to the user.
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "ATM Alert", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Transaction Successful", JOptionPane.INFORMATION_MESSAGE);
    }
}
