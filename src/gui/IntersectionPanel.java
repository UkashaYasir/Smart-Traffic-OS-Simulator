package gui;

import model.*;
import simulation.TrafficSimulationEngine;
import sync.DeadlockManager;
import sync.TrafficPhaser;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.Map;

/**
 * Custom painted panel that draws the traffic intersection, vehicles,
 * traffic lights with countdown timers, and deadlock overlays.
 * Uses Java2D for smooth rendering.
 * 
 * A Swing Timer triggers repaint() every 30ms for smooth animation.
 * Vehicle positions are read from volatile fields (thread-safe).
 */
public class IntersectionPanel extends JPanel {

    // Layout constants
    public static final int PANEL_SIZE = 700;
    public static final int ROAD_WIDTH = 120;
    public static final int LANE_WIDTH = ROAD_WIDTH / 2;

    // Calculated positions
    private static final int CENTER = PANEL_SIZE / 2;                    // 350
    private static final int ROAD_LEFT = CENTER - ROAD_WIDTH / 2;        // 290
    private static final int ROAD_RIGHT = CENTER + ROAD_WIDTH / 2;       // 410
    private static final int ROAD_TOP = CENTER - ROAD_WIDTH / 2;         // 290
    private static final int ROAD_BOTTOM = CENTER + ROAD_WIDTH / 2;      // 410

    // Colors
    private static final Color BG_COLOR = new Color(34, 40, 49);
    private static final Color GRASS_COLOR = new Color(39, 78, 46);
    private static final Color ROAD_COLOR = new Color(60, 63, 68);
    private static final Color ROAD_LINE_COLOR = new Color(200, 200, 50, 150);
    private static final Color INTERSECTION_COLOR = new Color(70, 73, 78);
    private static final Color SIDEWALK_COLOR = new Color(120, 115, 105);
    private static final Color LABEL_COLOR = new Color(200, 200, 220);
    private static final Color BLOCKED_COLOR = new Color(150, 150, 150);
    private static final Color DEADLOCK_WARNING = new Color(255, 60, 60, 80);

    private TrafficSimulationEngine engine;

    public IntersectionPanel() {
        setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));
        setBackground(BG_COLOR);
    }

    public void setEngine(TrafficSimulationEngine engine) {
        this.engine = engine;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Enable anti-aliasing for smooth rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        drawBackground(g2);
        drawRoads(g2);
        drawIntersection(g2);
        drawLaneMarkings(g2);

        if (engine != null) {
            drawStopLineIndicators(g2);
            drawTrafficLights(g2);
            drawVehicles(g2);
            drawDeadlockOverlay(g2);
        }

        drawDirectionLabels(g2);
        drawTitle(g2);
    }

    /**
     * Draws the grassy background.
     */
    private void drawBackground(Graphics2D g2) {
        g2.setColor(GRASS_COLOR);
        g2.fillRect(0, 0, PANEL_SIZE, PANEL_SIZE);
    }

    /**
     * Draws the four roads leading to the intersection.
     */
    private void drawRoads(Graphics2D g2) {
        g2.setColor(ROAD_COLOR);

        // Vertical road (North-South)
        g2.fillRect(ROAD_LEFT, 0, ROAD_WIDTH, PANEL_SIZE);

        // Horizontal road (East-West)
        g2.fillRect(0, ROAD_TOP, PANEL_SIZE, ROAD_WIDTH);

        // Sidewalks (thin borders along roads)
        g2.setColor(SIDEWALK_COLOR);
        g2.fillRect(ROAD_LEFT - 4, 0, 4, PANEL_SIZE);
        g2.fillRect(ROAD_RIGHT, 0, 4, PANEL_SIZE);
        g2.fillRect(0, ROAD_TOP - 4, PANEL_SIZE, 4);
        g2.fillRect(0, ROAD_BOTTOM, PANEL_SIZE, 4);
    }

    /**
     * Draws the intersection area.
     */
    private void drawIntersection(Graphics2D g2) {
        g2.setColor(INTERSECTION_COLOR);
        g2.fillRect(ROAD_LEFT, ROAD_TOP, ROAD_WIDTH, ROAD_WIDTH);
    }

    /**
     * Draws lane divider markings (dashed yellow lines).
     */
    private void drawLaneMarkings(Graphics2D g2) {
        g2.setColor(ROAD_LINE_COLOR);
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{15, 10}, 0));

        // Vertical center line (outside intersection)
        g2.drawLine(CENTER, 0, CENTER, ROAD_TOP);
        g2.drawLine(CENTER, ROAD_BOTTOM, CENTER, PANEL_SIZE);

        // Horizontal center line (outside intersection)
        g2.drawLine(0, CENTER, ROAD_LEFT, CENTER);
        g2.drawLine(ROAD_RIGHT, CENTER, PANEL_SIZE, CENTER);

        g2.setStroke(new BasicStroke(1));
    }

    /**
     * Draws colored stop-line indicators at each lane's stop line.
     * The color matches the traffic light state, making it immediately
     * clear which lane has green/red/yellow.
     */
    private void drawStopLineIndicators(Graphics2D g2) {
        Map<Direction, Lane> lanes = engine.getLanes();

        // NORTH stop line (right half of vertical road, at top of intersection)
        // Vehicles FROM north approach this line going south
        TrafficLight northLight = lanes.get(Direction.NORTH).getLight();
        drawStopLine(g2, northLight.getColor(),
                CENTER + 2, ROAD_TOP - 3, ROAD_RIGHT - 5, ROAD_TOP + 1, "horizontal");

        // SOUTH stop line (left half of vertical road, at bottom of intersection)
        // Vehicles FROM south approach this line going north
        TrafficLight southLight = lanes.get(Direction.SOUTH).getLight();
        drawStopLine(g2, southLight.getColor(),
                ROAD_LEFT + 5, ROAD_BOTTOM - 1, CENTER - 2, ROAD_BOTTOM + 3, "horizontal");

        // EAST stop line (top half of horizontal road, at right of intersection)
        // Vehicles FROM east approach this line going west
        TrafficLight eastLight = lanes.get(Direction.EAST).getLight();
        drawStopLine(g2, eastLight.getColor(),
                ROAD_RIGHT - 1, ROAD_TOP + 5, ROAD_RIGHT + 3, CENTER - 2, "vertical");

        // WEST stop line (bottom half of horizontal road, at left of intersection)
        // Vehicles FROM west approach this line going east
        TrafficLight westLight = lanes.get(Direction.WEST).getLight();
        drawStopLine(g2, westLight.getColor(),
                ROAD_LEFT - 3, CENTER + 2, ROAD_LEFT + 1, ROAD_BOTTOM - 5, "vertical");
    }

    /**
     * Draws a single colored stop line with glow effect.
     */
    private void drawStopLine(Graphics2D g2, Color color, int x1, int y1, int x2, int y2, String orientation) {
        // Glow effect
        Color glowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 50);
        g2.setColor(glowColor);
        if (orientation.equals("horizontal")) {
            g2.fillRect(x1 - 2, y1 - 4, x2 - x1 + 4, y2 - y1 + 8);
        } else {
            g2.fillRect(x1 - 4, y1 - 2, x2 - x1 + 8, y2 - y1 + 4);
        }

        // Solid line
        g2.setColor(color);
        g2.fillRect(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Draws traffic lights near each lane with direction labels and countdown timers.
     * Each light is positioned next to the lane it controls for clarity.
     */
    private void drawTrafficLights(Graphics2D g2) {
        Map<Direction, Lane> lanes = engine.getLanes();
        TrafficPhaser phaser = engine.getTrafficPhaser();
        int remaining = phaser.getRemainingSeconds();

        // ── NORTH light ──
        // Controls vehicles FROM north (going south)
        // Positioned: top-right corner, next to the north incoming lane
        drawTrafficLightUnit(g2,
                ROAD_RIGHT + 12,       // x: just right of road
                ROAD_TOP - 90,          // y: above intersection
                lanes.get(Direction.NORTH).getLight(),
                "NORTH", remaining);

        // ── SOUTH light ──
        // Controls vehicles FROM south (going north)
        // Positioned: bottom-left corner, next to the south incoming lane
        drawTrafficLightUnit(g2,
                ROAD_LEFT - 62,         // x: just left of road
                ROAD_BOTTOM + 18,       // y: below intersection
                lanes.get(Direction.SOUTH).getLight(),
                "SOUTH", remaining);

        // ── EAST light ──
        // Controls vehicles FROM east (going west)
        // Positioned: top-right area, next to the east incoming lane
        drawTrafficLightUnit(g2,
                ROAD_RIGHT + 12,        // x: just right of road
                ROAD_BOTTOM + 18,       // y: below intersection
                lanes.get(Direction.EAST).getLight(),
                "EAST", remaining);

        // ── WEST light ──
        // Controls vehicles FROM west (going east)
        // Positioned: top-left area, next to the west incoming lane
        drawTrafficLightUnit(g2,
                ROAD_LEFT - 62,         // x: just left of road
                ROAD_TOP - 90,          // y: above intersection
                lanes.get(Direction.WEST).getLight(),
                "WEST", remaining);
    }

    /**
     * Draws a single traffic light unit with housing, 3 lights, label, and timer.
     */
    private void drawTrafficLightUnit(Graphics2D g2, int x, int y,
                                       TrafficLight currentState,
                                       String directionLabel, int timerSeconds) {
        int w = 50, h = 70;

        // Background housing
        g2.setColor(new Color(30, 30, 35));
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(new Color(70, 70, 80));
        g2.drawRoundRect(x, y, w, h, 10, 10);

        // Direction label at top
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2.setColor(new Color(180, 200, 255));
        FontMetrics fm = g2.getFontMetrics();
        int labelWidth = fm.stringWidth(directionLabel);
        g2.drawString(directionLabel, x + (w - labelWidth) / 2, y + 13);

        // Three lights (Red, Yellow, Green) — arranged vertically
        int lightSize = 14;
        int lightX = x + (w - lightSize) / 2;
        int lightY = y + 18;
        int gap = 2;

        // Red light
        Color red = (currentState == TrafficLight.RED)
                ? TrafficLight.RED.getColor() : new Color(80, 30, 30);
        if (currentState == TrafficLight.RED) {
            // Active glow
            g2.setColor(new Color(231, 76, 60, 60));
            g2.fillOval(lightX - 3, lightY - 3, lightSize + 6, lightSize + 6);
        }
        g2.setColor(red);
        g2.fillOval(lightX, lightY, lightSize, lightSize);

        // Yellow light
        lightY += lightSize + gap;
        Color yellow = (currentState == TrafficLight.YELLOW)
                ? TrafficLight.YELLOW.getColor() : new Color(80, 70, 20);
        if (currentState == TrafficLight.YELLOW) {
            g2.setColor(new Color(241, 196, 15, 60));
            g2.fillOval(lightX - 3, lightY - 3, lightSize + 6, lightSize + 6);
        }
        g2.setColor(yellow);
        g2.fillOval(lightX, lightY, lightSize, lightSize);

        // Green light
        lightY += lightSize + gap;
        Color green = (currentState == TrafficLight.GREEN)
                ? TrafficLight.GREEN.getColor() : new Color(20, 60, 30);
        if (currentState == TrafficLight.GREEN) {
            g2.setColor(new Color(46, 204, 113, 60));
            g2.fillOval(lightX - 3, lightY - 3, lightSize + 6, lightSize + 6);
        }
        g2.setColor(green);
        g2.fillOval(lightX, lightY, lightSize, lightSize);

        // Countdown timer at bottom
        g2.setFont(new Font("Consolas", Font.BOLD, 14));
        String timerStr = timerSeconds + "s";
        int timerWidth = g2.getFontMetrics().stringWidth(timerStr);

        // Timer color matches current light state
        g2.setColor(currentState.getColor());
        g2.drawString(timerStr, x + (w - timerWidth) / 2, y + h - 4);
    }

    /**
     * Draws direction labels (N, S, E, W) at the edges of the roads.
     */
    private void drawDirectionLabels(Graphics2D g2) {
        g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g2.setColor(LABEL_COLOR);

        g2.drawString("N", CENTER - 7, 22);
        g2.drawString("S", CENTER - 6, PANEL_SIZE - 8);
        g2.drawString("E", PANEL_SIZE - 22, CENTER + 7);
        g2.drawString("W", 8, CENTER + 7);

        // Lane direction arrows (small arrows showing traffic flow)
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2.setColor(new Color(150, 150, 170, 120));

        // North lane: vehicles go south (down arrow)
        g2.drawString("\u2193", CENTER + LANE_WIDTH / 2 - 4, 50);
        // South lane: vehicles go north (up arrow)
        g2.drawString("\u2191", ROAD_LEFT + LANE_WIDTH / 2 - 4, PANEL_SIZE - 40);
        // East lane: vehicles go west (left arrow)
        g2.drawString("\u2190", PANEL_SIZE - 50, ROAD_TOP + LANE_WIDTH / 2 + 5);
        // West lane: vehicles go east (right arrow)
        g2.drawString("\u2192", 40, CENTER + LANE_WIDTH / 2 + 5);
    }

    /**
     * Draws all vehicles on the road.
     * Reads vehicle positions from volatile fields (thread-safe).
     */
    private void drawVehicles(Graphics2D g2) {
        List<Vehicle> vehicles = engine.getAllVehicles();

        for (Vehicle v : vehicles) {
            int vx = v.getX();
            int vy = v.getY();
            int vw, vh;

            // Vehicle dimensions depend on direction (horizontal vs vertical)
            if (v.getDirection() == Direction.NORTH || v.getDirection() == Direction.SOUTH) {
                vw = Vehicle.HEIGHT; // narrower width for vertical movement
                vh = Vehicle.WIDTH;  // longer height
            } else {
                vw = Vehicle.WIDTH;
                vh = Vehicle.HEIGHT;
            }

            // Vehicle color (gray if deadlocked/blocked)
            Color vehicleColor;
            if (v.isDeadlocked() || v.getStatus() == VehicleStatus.BLOCKED) {
                vehicleColor = BLOCKED_COLOR;
            } else {
                vehicleColor = v.getType().getColor();
            }

            // Draw vehicle body
            g2.setColor(vehicleColor);
            g2.fillRoundRect(vx, vy, vw, vh, 4, 4);

            // Draw vehicle border
            g2.setColor(vehicleColor.darker());
            g2.drawRoundRect(vx, vy, vw, vh, 4, 4);

            // Draw windshield effect
            g2.setColor(new Color(100, 150, 200, 120));
            if (v.getDirection() == Direction.NORTH || v.getDirection() == Direction.SOUTH) {
                g2.fillRect(vx + 2, vy + 2, vw - 4, 5);
            } else {
                g2.fillRect(vx + 2, vy + 2, 5, vh - 4);
            }

            // Draw emergency indicators (flashing siren)
            if (v.isEmergency()) {
                boolean flash = (System.currentTimeMillis() / 300) % 2 == 0;
                if (flash) {
                    g2.setColor(new Color(255, 0, 0, 80));
                    g2.fillOval(vx - 3, vy - 3, vw + 6, vh + 6);
                }
                g2.setColor(flash ? Color.RED : Color.BLUE);
                g2.fillOval(vx + vw / 2 - 3, vy + vh / 2 - 3, 6, 6);
            }

            // Draw vehicle ID
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
            g2.drawString("" + v.getVehicleId(), vx + 3, vy + vh - 3);
        }

        // Draw deadlock demo vehicles if active
        DeadlockManager dm = engine.getDeadlockManager();
        if (dm.isDeadlockTriggered()) {
            Vehicle da = dm.getDeadlockVehicleA();
            Vehicle db = dm.getDeadlockVehicleB();
            if (da != null) drawDeadlockVehicle(g2, da);
            if (db != null) drawDeadlockVehicle(g2, db);
        }
    }

    /**
     * Draws a vehicle involved in deadlock with special visual indicators.
     */
    private void drawDeadlockVehicle(Graphics2D g2, Vehicle v) {
        int vx = v.getX(), vy = v.getY();
        int vw = Vehicle.WIDTH, vh = Vehicle.HEIGHT;

        // Pulsing red glow
        boolean pulse = (System.currentTimeMillis() / 500) % 2 == 0;
        if (pulse) {
            g2.setColor(new Color(255, 50, 50, 80));
            g2.fillOval(vx - 10, vy - 10, vw + 20, vh + 20);
        }

        // Gray body (blocked)
        g2.setColor(BLOCKED_COLOR);
        g2.fillRoundRect(vx, vy, vw, vh, 4, 4);
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(vx, vy, vw, vh, 4, 4);
        g2.setStroke(new BasicStroke(1));

        // X mark
        g2.setColor(Color.RED);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString("X", vx + vw / 2 - 4, vy + vh / 2 + 4);

        // Label
        g2.setColor(Color.RED);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
        g2.drawString("BLOCKED", vx - 5, vy - 5);
    }

    /**
     * Draws deadlock warning overlay when deadlock is active.
     */
    private void drawDeadlockOverlay(Graphics2D g2) {
        if (engine.getDeadlockManager().isDeadlockActive()) {
            // Red tint over intersection
            g2.setColor(DEADLOCK_WARNING);
            g2.fillRect(ROAD_LEFT, ROAD_TOP, ROAD_WIDTH, ROAD_WIDTH);

            // Warning text
            boolean blink = (System.currentTimeMillis() / 600) % 2 == 0;
            if (blink) {
                g2.setColor(Color.RED);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String msg = "!! DEADLOCK DETECTED !!";
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(msg);
                g2.drawString(msg, CENTER - tw / 2, CENTER - 10);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                String sub = "Click 'Resolve Deadlock' to fix";
                tw = g2.getFontMetrics().stringWidth(sub);
                g2.drawString(sub, CENTER - tw / 2, CENTER + 10);
            }
        }
    }

    /**
     * Draws the title and status at the bottom.
     */
    private void drawTitle(Graphics2D g2) {
        g2.setColor(new Color(200, 200, 220, 180));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString("AI Traffic Intersection Simulator", 10, PANEL_SIZE - 10);

        // Status indicator
        if (engine != null) {
            String status = engine.isRunning()
                    ? (engine.isPaused() ? "|| PAUSED" : ">> RUNNING")
                    : "[] STOPPED";
            Color statusColor = engine.isRunning()
                    ? (engine.isPaused() ? new Color(241, 196, 15) : new Color(46, 204, 113))
                    : new Color(231, 76, 60);
            g2.setColor(statusColor);
            g2.drawString(status, PANEL_SIZE - 110, PANEL_SIZE - 10);
        }
    }
}
