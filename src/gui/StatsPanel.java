package gui;

import simulation.StatisticsManager;
import simulation.TrafficSimulationEngine;
import sync.TrafficPhaser;
import model.Direction;
import model.Lane;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Map;

/**
 * Panel that displays real-time simulation statistics.
 * Updated periodically by a Swing Timer.
 */
public class StatsPanel extends JPanel {

    private final JLabel totalGeneratedLabel;
    private final JLabel vehiclesPassedLabel;
    private final JLabel vehiclesWaitingLabel;
    private final JLabel emergencyLabel;
    private final JLabel avgWaitLabel;
    private final JLabel maxWaitLabel;
    private final JLabel deadlockCountLabel;
    private final JLabel activeLaneLabel;
    private final JLabel activePhaseLabel;
    private final JLabel semaphoreLabel;
    private final JLabel aiDecisionLabel;

    private TrafficSimulationEngine engine;

    public StatsPanel() {
        setLayout(new GridLayout(0, 1, 2, 2));
        setPreferredSize(new Dimension(380, 320));
        setBackground(new Color(25, 25, 35));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        // Title
        JLabel titleLabel = createLabel("📊 Statistics Dashboard", new Color(120, 255, 180), true);
        add(titleLabel);

        // Stats labels
        totalGeneratedLabel = createLabel("Total Vehicles: 0", new Color(200, 200, 220), false);
        vehiclesPassedLabel = createLabel("Vehicles Passed: 0", new Color(200, 200, 220), false);
        vehiclesWaitingLabel = createLabel("Vehicles Waiting: 0", new Color(200, 200, 220), false);
        emergencyLabel = createLabel("Emergency Handled: 0", new Color(200, 200, 220), false);
        avgWaitLabel = createLabel("Avg Wait Time: 0ms", new Color(200, 200, 220), false);
        maxWaitLabel = createLabel("Max Wait Time: 0ms", new Color(200, 200, 220), false);
        deadlockCountLabel = createLabel("Deadlock Count: 0", new Color(200, 200, 220), false);
        activeLaneLabel = createLabel("Active Lane: N-S", new Color(255, 200, 100), false);
        activePhaseLabel = createLabel("Phase: N-S Green", new Color(255, 200, 100), false);
        semaphoreLabel = createLabel("Semaphore: 2/2", new Color(180, 180, 255), false);
        aiDecisionLabel = createLabel("AI: Idle", new Color(100, 255, 200), false);

        add(totalGeneratedLabel);
        add(vehiclesPassedLabel);
        add(vehiclesWaitingLabel);
        add(emergencyLabel);
        add(avgWaitLabel);
        add(maxWaitLabel);
        add(deadlockCountLabel);
        add(createSeparator());
        add(activeLaneLabel);
        add(activePhaseLabel);
        add(semaphoreLabel);
        add(aiDecisionLabel);
    }

    public void setEngine(TrafficSimulationEngine engine) {
        this.engine = engine;
    }

    /**
     * Updates all statistics labels. Called by GUI timer.
     */
    public void updateStats() {
        if (engine == null) return;

        StatisticsManager stats = engine.getStats();
        TrafficPhaser phaser = engine.getTrafficPhaser();

        totalGeneratedLabel.setText("  Total Vehicles: " + stats.getTotalGenerated());
        vehiclesPassedLabel.setText("  Vehicles Passed: " + stats.getVehiclesPassed());
        vehiclesWaitingLabel.setText("  Vehicles Waiting: " + stats.getVehiclesWaiting());
        emergencyLabel.setText("  Emergency Handled: " + stats.getEmergencyHandled());
        avgWaitLabel.setText("  Avg Wait Time: " + stats.getAverageWaitTime() + "ms");
        maxWaitLabel.setText("  Max Wait Time: " + stats.getMaxWaitTime() + "ms");
        deadlockCountLabel.setText("  Deadlock Count: " + engine.getDeadlockManager().getDeadlockCount());
        activePhaseLabel.setText("  Phase: " + phaser.getCurrentPhaseName());
        semaphoreLabel.setText("  Semaphore: " + engine.getSemaphore().getAvailablePermits()
                + "/" + engine.getSemaphore().getMaxPermits());

        // Determine active lane
        String activeLane = phaser.isNorthSouthGreen() ? "North-South" :
                           phaser.isEastWestGreen() ? "East-West" : "Transitioning";
        activeLaneLabel.setText("  Active Lane: " + activeLane);

        // AI decision
        aiDecisionLabel.setText("  AI: " + engine.getAIController().getDecisionSummary());
    }

    private JLabel createLabel(String text, Color color, boolean isTitle) {
        JLabel label = new JLabel("  " + text);
        label.setFont(new Font("Segoe UI", isTitle ? Font.BOLD : Font.PLAIN, isTitle ? 14 : 12));
        label.setForeground(color);
        return label;
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 60, 80));
        return sep;
    }
}
