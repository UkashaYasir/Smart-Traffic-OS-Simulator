import gui.TrafficSimulatorGUI;
import javax.swing.*;

/**
 * ============================================================
 * AI-Based Smart Traffic Control System
 * Using OS Synchronization Concepts
 * ============================================================
 * 
 * 4th Semester BS AI — Operating System Project
 * 
 * OS Concepts Demonstrated:
 * 1. Semaphore        — Controls intersection (critical section) access
 * 2. CountDownLatch   — Synchronized start of all vehicle threads
 * 3. CyclicBarrier    — Convoy group movement synchronization
 * 4. Phaser           — Traffic signal multi-phase control
 * 5. Exchanger        — Inter-vehicle congestion data exchange
 * 6. Deadlock         — Demonstration and resolution
 * 
 * AI Component:
 * Rule-based smart traffic controller adjusts signal timing
 * based on traffic load, emergency vehicles, and starvation.
 * 
 * How to Run:
 * 1. Open terminal/command prompt
 * 2. Navigate to project root folder
 * 3. Compile:  javac -d out src/Main.java src/model/*.java src/sync/*.java
 *              src/ai/*.java src/simulation/*.java src/gui/*.java src/utils/*.java
 * 4. Run:      java -cp out Main
 * 
 * ============================================================
 */
public class Main {

    public static void main(String[] args) {
        // Set look and feel to system default for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to default look and feel
        }

        // Set dark theme defaults for Swing components
        UIManager.put("Panel.background", new java.awt.Color(25, 25, 35));
        UIManager.put("OptionPane.background", new java.awt.Color(25, 25, 35));
        UIManager.put("OptionPane.messageForeground", java.awt.Color.WHITE);

        // Launch GUI on the Event Dispatch Thread (EDT)
        // This is REQUIRED for thread-safe Swing operations
        SwingUtilities.invokeLater(() -> {
            TrafficSimulatorGUI gui = new TrafficSimulatorGUI();
            gui.setVisible(true);
        });
    }
}
