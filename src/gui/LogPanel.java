package gui;

import utils.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * Panel that displays real-time simulation logs.
 * Implements Logger.LogListener to receive log messages from any thread.
 * Uses SwingUtilities.invokeLater for thread-safe GUI updates.
 */
public class LogPanel extends JPanel implements Logger.LogListener {

    private final JTextPane logPane;
    private final StyledDocument doc;
    private final JScrollPane scrollPane;

    public LogPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(380, 300));
        setBackground(new Color(25, 25, 35));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Title label
        JLabel titleLabel = new JLabel("  📋 Simulation Logs");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(120, 180, 255));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(30, 30, 45));
        titleLabel.setPreferredSize(new Dimension(0, 30));
        add(titleLabel, BorderLayout.NORTH);

        // Log text pane (rich text)
        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setBackground(new Color(18, 18, 28));
        logPane.setCaretColor(new Color(100, 150, 255));
        doc = logPane.getStyledDocument();

        // Setup Styles
        Style defaultStyle = logPane.addStyle("Default", null);
        StyleConstants.setForeground(defaultStyle, new Color(200, 200, 220));
        StyleConstants.setFontFamily(defaultStyle, "Consolas");
        StyleConstants.setFontSize(defaultStyle, 11);

        Style highlightStyle = logPane.addStyle("Highlight", null);
        StyleConstants.setForeground(highlightStyle, new Color(120, 255, 180)); // Light Green/Cyan
        StyleConstants.setFontFamily(highlightStyle, "Consolas");
        StyleConstants.setFontSize(highlightStyle, 11);

        Style errorStyle = logPane.addStyle("Error", null);
        StyleConstants.setForeground(errorStyle, new Color(255, 100, 100)); // Red
        StyleConstants.setFontFamily(errorStyle, "Consolas");
        StyleConstants.setFontSize(errorStyle, 11);

        Style successStyle = logPane.addStyle("Success", null);
        StyleConstants.setForeground(successStyle, new Color(241, 196, 15)); // Yellow/Gold
        StyleConstants.setFontFamily(successStyle, "Consolas");
        StyleConstants.setFontSize(successStyle, 11);

        scrollPane = new JScrollPane(logPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setBackground(new Color(30, 30, 45));
        add(scrollPane, BorderLayout.CENTER);

        // Clear button
        JButton clearBtn = new JButton("Clear Logs");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearBtn.setBackground(new Color(60, 60, 80));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setFocusPainted(false);
        clearBtn.setBorderPainted(false);
        clearBtn.addActionListener(e -> {
            logPane.setText("");
            Logger.getInstance().clear();
        });
        add(clearBtn, BorderLayout.SOUTH);

        // Register as log listener
        Logger.getInstance().addListener(this);
    }

    /**
     * Called by Logger when a new log message is available.
     * Uses invokeLater for thread-safe Swing update.
     */
    @Override
    public void onLogMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                Style style = logPane.getStyle("Default");
                String lowerMsg = message.toLowerCase();
                
                if (lowerMsg.contains("deadlock") || lowerMsg.contains("error") || lowerMsg.contains("blocked")) {
                    style = logPane.getStyle("Error");
                } else if (lowerMsg.contains("ai") || lowerMsg.contains("convoy") || lowerMsg.contains("phase")) {
                    style = logPane.getStyle("Highlight");
                } else if (lowerMsg.contains("resolved") || lowerMsg.contains("passed") || lowerMsg.contains("exchange")) {
                    style = logPane.getStyle("Success");
                }
                
                doc.insertString(doc.getLength(), message + "\n", style);
                logPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                // Ignore exception on insert
            }
        });
    }
}
