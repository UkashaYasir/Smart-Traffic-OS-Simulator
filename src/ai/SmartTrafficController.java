package ai;

import model.Direction;
import model.Lane;
import sync.TrafficPhaser;
import utils.Logger;
import java.util.Map;

/**
 * AI Component: Rule-Based Smart Traffic Controller
 * 
 * Uses simple rules to make intelligent decisions about traffic light timing.
 * This simulates how AI can optimize traffic flow at intersections.
 * 
 * Rules:
 * 1. If one lane has more vehicles, give it longer green time.
 * 2. If an emergency vehicle appears, give it highest priority.
 * 3. If a lane waits too long, increase its priority.
 * 4. If deadlock risk appears, prevent unsafe locking.
 * 5. If all lanes are balanced, rotate signals normally.
 */
public class SmartTrafficController {

    private final Logger logger = Logger.getInstance();
    private static final int BASE_GREEN_TIME = 5000;   // 5 seconds base
    private static final int MIN_GREEN_TIME = 3000;    // 3 seconds minimum
    private static final int MAX_GREEN_TIME = 10000;   // 10 seconds maximum
    private static final long STARVATION_THRESHOLD = 15000; // 15 seconds

    private volatile boolean emergencyOverride = false;
    private volatile Direction emergencyDirection = null;

    /**
     * Analyzes current traffic conditions and adjusts signal timing.
     * Called periodically by the simulation engine.
     * 
     * @param lanes the four traffic lanes
     * @param phaser the traffic signal phaser to adjust
     */
    public void analyze(Map<Direction, Lane> lanes, TrafficPhaser phaser) {
        // Rule 2: Emergency vehicle priority (highest priority rule)
        if (checkEmergencyVehicles(lanes, phaser)) return;

        // Rule 3: Starvation prevention
        if (checkStarvation(lanes, phaser)) return;

        // Rule 1: Load-based timing adjustment
        adjustForTrafficLoad(lanes, phaser);
    }

    /**
     * Rule 2: Emergency Vehicle Priority
     * If an emergency vehicle is detected, immediately give its direction green.
     */
    private boolean checkEmergencyVehicles(Map<Direction, Lane> lanes, TrafficPhaser phaser) {
        for (Direction dir : Direction.values()) {
            Lane lane = lanes.get(dir);
            if (lane.hasEmergencyVehicle()) {
                emergencyOverride = true;
                emergencyDirection = dir;

                logger.log("🚨 AI: Emergency vehicle detected in " + dir.getDisplayName()
                        + " lane! Activating priority mode.");

                // Give emergency direction maximum green time
                if (dir.isNorthSouth()) {
                    phaser.setGreenDurationNS(MAX_GREEN_TIME);
                    phaser.setGreenDurationEW(MIN_GREEN_TIME);
                } else {
                    phaser.setGreenDurationEW(MAX_GREEN_TIME);
                    phaser.setGreenDurationNS(MIN_GREEN_TIME);
                }

                logger.log("🚨 AI: Priority green assigned to " + dir.getDisplayName());
                return true;
            }
        }

        // Clear emergency override if no emergency vehicles
        if (emergencyOverride) {
            emergencyOverride = false;
            emergencyDirection = null;
            logger.log("🚨 AI: Emergency priority cleared. Returning to normal.");
        }
        return false;
    }

    /**
     * Rule 3: Starvation Prevention
     * If any lane has been waiting too long, boost its priority.
     */
    private boolean checkStarvation(Map<Direction, Lane> lanes, TrafficPhaser phaser) {
        long maxWait = 0;
        Direction starvedDir = null;

        for (Direction dir : Direction.values()) {
            long wait = lanes.get(dir).getMaxWaitTime();
            if (wait > maxWait) {
                maxWait = wait;
                starvedDir = dir;
            }
        }

        if (maxWait > STARVATION_THRESHOLD && starvedDir != null) {
            logger.log("⏰ AI: Lane " + starvedDir.getDisplayName()
                    + " starving (waited " + (maxWait / 1000) + "s). Boosting priority.");

            if (starvedDir.isNorthSouth()) {
                phaser.setGreenDurationNS(Math.min(phaser.getGreenDurationNS() + 2000, MAX_GREEN_TIME));
            } else {
                phaser.setGreenDurationEW(Math.min(phaser.getGreenDurationEW() + 2000, MAX_GREEN_TIME));
            }
            return true;
        }
        return false;
    }

    /**
     * Rule 1: Load-Based Timing Adjustment
     * Gives more green time to lanes with more vehicles.
     * Rule 5: If balanced, use default timing.
     */
    private void adjustForTrafficLoad(Map<Direction, Lane> lanes, TrafficPhaser phaser) {
        int nsCount = lanes.get(Direction.NORTH).getVehicleCount()
                    + lanes.get(Direction.SOUTH).getVehicleCount();
        int ewCount = lanes.get(Direction.EAST).getVehicleCount()
                    + lanes.get(Direction.WEST).getVehicleCount();

        int totalCount = nsCount + ewCount;
        if (totalCount == 0) {
            // Rule 5: No vehicles, use default timing
            phaser.setGreenDurationNS(BASE_GREEN_TIME);
            phaser.setGreenDurationEW(BASE_GREEN_TIME);
            return;
        }

        // Calculate proportional green time
        double nsRatio = (double) nsCount / totalCount;
        double ewRatio = (double) ewCount / totalCount;

        int nsGreen = (int) Math.max(MIN_GREEN_TIME, Math.min(MAX_GREEN_TIME,
                BASE_GREEN_TIME * 2 * nsRatio));
        int ewGreen = (int) Math.max(MIN_GREEN_TIME, Math.min(MAX_GREEN_TIME,
                BASE_GREEN_TIME * 2 * ewRatio));

        // Only log if there's a significant difference
        if (Math.abs(nsCount - ewCount) > 2) {
            logger.log("🤖 AI: Load adjusted — N-S(" + nsCount + " vehicles): "
                    + nsGreen + "ms | E-W(" + ewCount + " vehicles): " + ewGreen + "ms");
        }

        phaser.setGreenDurationNS(nsGreen);
        phaser.setGreenDurationEW(ewGreen);
    }

    /**
     * Rule 4: Deadlock Risk Assessment
     * Checks if conditions might lead to deadlock.
     */
    public boolean assessDeadlockRisk(Map<Direction, Lane> lanes) {
        // Check if all four lanes have waiting vehicles (potential circular wait)
        boolean allLanesOccupied = true;
        for (Direction dir : Direction.values()) {
            if (lanes.get(dir).getWaitingCount() == 0) {
                allLanesOccupied = false;
                break;
            }
        }

        if (allLanesOccupied) {
            logger.log("⚠ AI Rule 4: All lanes occupied — potential deadlock risk detected!");
            return true;
        }
        return false;
    }

    /**
     * Returns the current AI decision summary for display.
     */
    public String getDecisionSummary() {
        if (emergencyOverride) {
            return "EMERGENCY: Priority to " + emergencyDirection.getDisplayName();
        }
        return "Normal: Adaptive timing active";
    }

    public boolean isEmergencyOverride() { return emergencyOverride; }
    public Direction getEmergencyDirection() { return emergencyDirection; }
}
