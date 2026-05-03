package sync;

import java.util.concurrent.Phaser;
import model.Direction;
import model.TrafficLight;
import model.Lane;
import utils.Logger;
import java.util.Map;

/**
 * OS Concept: PHASER
 * 
 * Controls traffic signal phase transitions using a multi-phase synchronization barrier.
 * 
 * A Phaser is a more flexible version of CyclicBarrier. It supports multiple 
 * sequential phases and dynamic party registration. In this project, it manages
 * the cyclic state machine of the traffic intersection.
 * 
 * Signal Cycle (Finite State Machine):
 * - Phase 0: North-South GREEN, East-West RED (Flow starts N-S)
 * - Phase 1: North-South YELLOW (Graceful transition/flushing)
 * - Phase 2: East-West GREEN, North-South RED (Flow starts E-W)
 * - Phase 3: East-West YELLOW (Graceful transition/flushing)
 * 
 * Each transition advances the internal Phaser counter, acting as a global clock.
 */
public class TrafficPhaser {

    private final Phaser phaser;
    private final Logger logger = Logger.getInstance();
    private volatile int currentPhaseIndex = 0; // 0-3
    private volatile int greenDurationNS = 5000; // Green time for N-S in ms
    private volatile int greenDurationEW = 5000; // Green time for E-W in ms
    private static final int YELLOW_DURATION = 1500; // Yellow time in ms
    private volatile long phaseStartTime = System.currentTimeMillis(); // When current phase began

    // Phase names for display
    private static final String[] PHASE_NAMES = {
        "N-S Green", "N-S Yellow", "E-W Green", "E-W Yellow"
    };

    public TrafficPhaser() {
        // Register 1 party (the signal controller thread)
        this.phaser = new Phaser(1);
    }

    /**
     * Returns the current phase name.
     */
    public String getCurrentPhaseName() {
        return PHASE_NAMES[currentPhaseIndex % 4];
    }

    /**
     * Returns the current phase index (0-3).
     */
    public int getCurrentPhaseIndex() {
        return currentPhaseIndex;
    }

    /**
     * Advances to the next traffic signal phase.
     * Updates traffic lights for all lanes accordingly.
     */
    public void advancePhase(Map<Direction, Lane> lanes) {
        currentPhaseIndex = (currentPhaseIndex + 1) % 4;
        phaseStartTime = System.currentTimeMillis(); // Record when this phase started

        switch (currentPhaseIndex) {
            case 0: // N-S Green
                lanes.get(Direction.NORTH).setLight(TrafficLight.GREEN);
                lanes.get(Direction.SOUTH).setLight(TrafficLight.GREEN);
                lanes.get(Direction.EAST).setLight(TrafficLight.RED);
                lanes.get(Direction.WEST).setLight(TrafficLight.RED);
                logger.log("🚦 Phase: N-S GREEN | E-W RED");
                break;
            case 1: // N-S Yellow
                lanes.get(Direction.NORTH).setLight(TrafficLight.YELLOW);
                lanes.get(Direction.SOUTH).setLight(TrafficLight.YELLOW);
                logger.log("🚦 Phase: N-S YELLOW | E-W RED");
                break;
            case 2: // E-W Green
                lanes.get(Direction.NORTH).setLight(TrafficLight.RED);
                lanes.get(Direction.SOUTH).setLight(TrafficLight.RED);
                lanes.get(Direction.EAST).setLight(TrafficLight.GREEN);
                lanes.get(Direction.WEST).setLight(TrafficLight.GREEN);
                logger.log("🚦 Phase: E-W GREEN | N-S RED");
                break;
            case 3: // E-W Yellow
                lanes.get(Direction.EAST).setLight(TrafficLight.YELLOW);
                lanes.get(Direction.WEST).setLight(TrafficLight.YELLOW);
                logger.log("🚦 Phase: E-W YELLOW | N-S RED");
                break;
        }

        // Advance the phaser
        phaser.arrive();
    }

    /**
     * Returns the duration of the current phase in milliseconds.
     */
    public int getCurrentPhaseDuration() {
        switch (currentPhaseIndex) {
            case 0: return greenDurationNS;
            case 1: return YELLOW_DURATION;
            case 2: return greenDurationEW;
            case 3: return YELLOW_DURATION;
            default: return 3000;
        }
    }

    /**
     * Sets green duration for North-South direction.
     */
    public void setGreenDurationNS(int ms) {
        this.greenDurationNS = ms;
    }

    /**
     * Sets green duration for East-West direction.
     */
    public void setGreenDurationEW(int ms) {
        this.greenDurationEW = ms;
    }

    /**
     * Gets the current phaser phase number (total phases elapsed).
     */
    public int getPhaserPhase() {
        return phaser.getPhase();
    }

    /**
     * Checks if N-S lanes currently have green light.
     */
    public boolean isNorthSouthGreen() {
        return currentPhaseIndex == 0;
    }

    /**
     * Checks if E-W lanes currently have green light.
     */
    public boolean isEastWestGreen() {
        return currentPhaseIndex == 2;
    }

    /**
     * Returns the remaining seconds in the current phase.
     * Used by GUI to display countdown timer.
     */
    public int getRemainingSeconds() {
        long elapsed = System.currentTimeMillis() - phaseStartTime;
        int duration = getCurrentPhaseDuration();
        int remainingMs = (int)(duration - elapsed);
        return Math.max(0, (remainingMs + 999) / 1000); // Round up to seconds
    }

    /**
     * Resets the phaser to initial state.
     */
    public void reset() {
        currentPhaseIndex = 0;
        greenDurationNS = 5000;
        greenDurationEW = 5000;
        phaseStartTime = System.currentTimeMillis();
    }

    public int getGreenDurationNS() { return greenDurationNS; }
    public int getGreenDurationEW() { return greenDurationEW; }
}
