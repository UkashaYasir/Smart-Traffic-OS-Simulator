package gui;

import utils.Logger;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Panel that displays real-time simulation logs.
 * Implements Logger.LogListener to receive log messages from any thread.
 * Uses SwingUtilities.invokeLater for thread-safe GUI updates.
 */
public class LogPanel extends JPanel implements Logger.LogListener {

    private final JTextArea logArea;
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

        // Log text area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(18, 18, 28));
        logArea.setForeground(new Color(200, 200, 220));
        logArea.setCaretColor(new Color(100, 150, 255));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        scrollPane = new JScrollPane(logArea);
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
            logArea.setText("");
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
            logArea.append(message + "\n");
            // Auto-scroll to bottom
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
