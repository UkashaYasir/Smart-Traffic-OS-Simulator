package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Educational panel designed to help newbies understand OS Synchronization Concepts
 * by visually explaining them in the context of the traffic simulation.
 */
public class OSConceptsPanel extends JPanel {

    public OSConceptsPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 30));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("🎓 OS Concepts Explorer");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(120, 255, 180));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(20, 20, 30));

        // Add Concept Cards
        contentPanel.add(createConceptCard(
            "1. Semaphore (Intersection Access)",
            "Prevents Collisions",
            "A Semaphore acts like a bouncer. It maintains a set number of permits. In our simulation, it strictly limits how many vehicles can enter the middle of the intersection at once to prevent crashes (Critical Section).",
            new Color(180, 180, 255)
        ));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        contentPanel.add(createConceptCard(
            "2. CountDownLatch (Start Sync)",
            "Race Starting Gun",
            "Ensures that no vehicle thread starts moving until the simulation engine is completely initialized. All vehicles 'wait' at the latch until it counts down to zero.",
            new Color(100, 200, 255)
        ));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        contentPanel.add(createConceptCard(
            "3. CyclicBarrier (Convoy Sync)",
            "Group Movement",
            "Used to form a convoy. Vehicles wait at a 'barrier' until a specified number of vehicles arrive. Once they all arrive, the barrier opens and they move through together.",
            new Color(255, 150, 200)
        ));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        contentPanel.add(createConceptCard(
            "4. Phaser (Traffic Lights)",
            "Multi-phase Control",
            "Manages the complex, multi-stage transitions of traffic lights. It cleanly synchronizes the shifting from Green to Yellow, and then to Red, across all lanes in phases.",
            new Color(255, 200, 100)
        ));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        contentPanel.add(createConceptCard(
            "5. Exchanger (Data Swap)",
            "Inter-vehicle Communication",
            "Provides a synchronization point where two vehicle threads can safely swap data. They use this to exchange congestion levels when passing each other.",
            new Color(155, 255, 155)
        ));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        contentPanel.add(createConceptCard(
            "6. Deadlock (Traffic Jam)",
            "Circular Wait & Resolution",
            "A Deadlock occurs when vehicles from all 4 directions block each other (a circular wait). The AI Controller detects this deadlock and resolves it by forcefully clearing one lane.",
            new Color(255, 100, 100)
        ));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setBackground(new Color(25, 25, 35));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createConceptCard(String title, String subtitle, String desc, Color themeColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(new Color(30, 30, 45));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(themeColor);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        subLbl.setForeground(new Color(180, 180, 200));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titleLbl, BorderLayout.WEST);
        header.add(subLbl, BorderLayout.EAST);

        JTextArea descArea = new JTextArea(desc);
        descArea.setOpaque(false);
        descArea.setForeground(new Color(220, 220, 220));
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setFocusable(false);

        card.add(header, BorderLayout.NORTH);
        card.add(descArea, BorderLayout.CENTER);

        return card;
    }
}
