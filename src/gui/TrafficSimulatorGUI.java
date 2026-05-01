package gui;

import simulation.TrafficSimulationEngine;
import javax.swing.*;
import java.awt.*;

/**
 * Main GUI frame that assembles all panels into the application window.
 * 
 * Layout:
 * ┌────────────────────────────────────────────────────────┐
 * │                    Title Bar                           │
 * ├──────────┬──────────────────────┬──────────────────────┤
 * │          │                      │     Stats Panel      │
 * │ Control  │   Intersection       ├──────────────────────┤
 * │  Panel   │      Panel           │      Log Panel       │
 * │          │                      │                      │
 * └──────────┴──────────────────────┴──────────────────────┘
 * 
 * A Swing Timer updates the GUI at ~30fps for smooth animation.
 */
public class TrafficSimulatorGUI extends JFrame {

    private final TrafficSimulationEngine engine;
    private final IntersectionPanel intersectionPanel;
    private final ControlPanel controlPanel;
    private final StatsPanel statsPanel;
    private final LogPanel logPanel;
    private Timer animationTimer;

    public TrafficSimulatorGUI() {
        // Initialize the simulation engine
        engine = new TrafficSimulationEngine();

        // Initialize panels
        intersectionPanel = new IntersectionPanel();
        intersectionPanel.setEngine(engine);

        controlPanel = new ControlPanel(engine);

        statsPanel = new StatsPanel();
        statsPanel.setEngine(engine);

        logPanel = new LogPanel();

        // Setup frame
        setTitle("AI-Based Smart Traffic Control System — OS Synchronization Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Build layout
        buildLayout();

        // Start animation timer (30fps)
        startAnimationTimer();

        // Pack and center
        pack();
        setLocationRelativeTo(null);

        // Initial log message
        utils.Logger.getInstance().log("════════════════════════════════════");
        utils.Logger.getInstance().log("AI-Based Smart Traffic Control System");
        utils.Logger.getInstance().log("OS Synchronization Concepts Demo");
        utils.Logger.getInstance().log("════════════════════════════════════");
        utils.Logger.getInstance().log("OS Concepts Used:");
        utils.Logger.getInstance().log("  1. Semaphore — Intersection access control");
        utils.Logger.getInstance().log("  2. CountDownLatch — Synchronized start");
        utils.Logger.getInstance().log("  3. CyclicBarrier — Convoy movement");
        utils.Logger.getInstance().log("  4. Phaser — Traffic signal phases");
        utils.Logger.getInstance().log("  5. Exchanger — Vehicle data exchange");
        utils.Logger.getInstance().log("  6. Deadlock — Demo & resolution");
        utils.Logger.getInstance().log("════════════════════════════════════");
        utils.Logger.getInstance().log("Click 'Start' to begin simulation.");
    }

    /**
     * Builds the main window layout using BorderLayout and nested panels.
     */
    private void buildLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(new Color(20, 20, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Title bar
        JPanel titleBar = createTitleBar();
        mainPanel.add(titleBar, BorderLayout.NORTH);

        // Left: Control panel
        mainPanel.add(controlPanel, BorderLayout.WEST);

        // Center: Intersection panel
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(new Color(20, 20, 30));
        centerWrapper.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 1));
        centerWrapper.add(intersectionPanel, BorderLayout.CENTER);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        // Right: Stats + Logs stacked vertically
        JPanel rightPanel = new JPanel(new BorderLayout(0, 5));
        rightPanel.setBackground(new Color(20, 20, 30));
        rightPanel.add(statsPanel, BorderLayout.NORTH);
        rightPanel.add(logPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        setContentPane(mainPanel);
    }

    /**
     * Creates the application title bar.
     */
    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(25, 25, 40));
        titleBar.setPreferredSize(new Dimension(0, 45));
        titleBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        JLabel titleLabel = new JLabel("🚦 AI-Based Smart Traffic Control System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(100, 200, 255));
        titleBar.add(titleLabel, BorderLayout.WEST);

        JLabel subtitleLabel = new JLabel("Using OS Synchronization Concepts  ");
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        subtitleLabel.setForeground(new Color(150, 150, 180));
        titleBar.add(subtitleLabel, BorderLayout.EAST);

        return titleBar;
    }

    /**
     * Starts the animation timer that repaints the intersection
     * and updates statistics at ~30fps.
     */
    private void startAnimationTimer() {
        animationTimer = new Timer(33, e -> {
            intersectionPanel.repaint();
            statsPanel.updateStats();
        });
        animationTimer.start();
    }
}
