package gui;

import simulation.TrafficSimulationEngine;
import utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Panel containing all control buttons for the simulation.
 * Provides Start, Pause, Resume, Stop, Add Vehicle, Deadlock, and Reset controls.
 */
public class ControlPanel extends JPanel {

    private final TrafficSimulationEngine engine;

    private JButton startBtn, pauseBtn, resumeBtn, stopBtn;
    private JButton addNormalBtn, addEmergencyBtn;
    private JButton triggerDeadlockBtn, resolveDeadlockBtn;
    private JButton resetBtn;
    private JButton exchangerDemoBtn, convoyDemoBtn;

    public ControlPanel(TrafficSimulationEngine engine) {
        this.engine = engine;
        setLayout(new GridLayout(0, 1, 4, 4));
        setPreferredSize(new Dimension(200, 0));
        setBackground(new Color(30, 30, 45));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Title
        JLabel title = new JLabel("🎛 Controls", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(120, 180, 255));
        add(title);

        // Simulation controls
        add(createSectionLabel("── Simulation ──"));
        startBtn = createButton("▶ Start", new Color(46, 204, 113), e -> {
            engine.start();
            updateButtonStates();
        });
        pauseBtn = createButton("⏸ Pause", new Color(241, 196, 15), e -> {
            engine.pause();
            updateButtonStates();
        });
        resumeBtn = createButton("▶ Resume", new Color(52, 152, 219), e -> {
            engine.resume();
            updateButtonStates();
        });
        stopBtn = createButton("⏹ Stop", new Color(231, 76, 60), e -> {
            engine.stop();
            updateButtonStates();
        });
        add(startBtn);
        add(pauseBtn);
        add(resumeBtn);
        add(stopBtn);

        // Vehicle controls
        add(createSectionLabel("── Vehicles ──"));
        addNormalBtn = createButton("🚗 Add Normal", new Color(52, 152, 219), e -> {
            engine.addNormalVehicle();
        });
        addEmergencyBtn = createButton("🚑 Add Emergency", new Color(231, 76, 60), e -> {
            engine.addEmergencyVehicle();
        });
        add(addNormalBtn);
        add(addEmergencyBtn);

        // Deadlock controls
        add(createSectionLabel("── Deadlock ──"));
        triggerDeadlockBtn = createButton("⚠ Trigger Deadlock", new Color(243, 156, 18), e -> {
            engine.triggerDeadlock();
        });
        resolveDeadlockBtn = createButton("✓ Resolve Deadlock", new Color(46, 204, 113), e -> {
            engine.resolveDeadlock();
        });
        add(triggerDeadlockBtn);
        add(resolveDeadlockBtn);

        // Sync demos
        add(createSectionLabel("── OS Demos ──"));
        exchangerDemoBtn = createButton("↔ Exchanger Demo", new Color(155, 89, 182), e -> {
            engine.demonstrateExchanger();
        });
        convoyDemoBtn = createButton("≡ Convoy Demo", new Color(155, 89, 182), e -> {
            engine.demonstrateConvoy();
        });
        add(exchangerDemoBtn);
        add(convoyDemoBtn);

        // Reset
        add(Box.createVerticalStrut(5));
        resetBtn = createButton("🔄 Reset", new Color(149, 165, 166), e -> {
            engine.reset();
            updateButtonStates();
        });
        add(resetBtn);

        updateButtonStates();
    }

    /**
     * Creates a styled button with the given properties.
     */
    private JButton createButton(String text, Color bgColor, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 32));
        btn.addActionListener(action);

        // Hover effect
        Color hoverColor = bgColor.brighter();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    /**
     * Creates a section label for organizing button groups.
     */
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        label.setForeground(new Color(100, 100, 130));
        return label;
    }

    /**
     * Updates button enabled states based on simulation state.
     */
    private void updateButtonStates() {
        boolean running = engine.isRunning();
        boolean paused = engine.isPaused();

        startBtn.setEnabled(!running);
        pauseBtn.setEnabled(running && !paused);
        resumeBtn.setEnabled(running && paused);
        stopBtn.setEnabled(running);
        addNormalBtn.setEnabled(true);
        addEmergencyBtn.setEnabled(true);
    }
}
