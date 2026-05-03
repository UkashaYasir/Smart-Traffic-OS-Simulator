package model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a vehicle in the traffic simulation.
 * Each vehicle has a position, direction, type, and status.
 * Position fields are volatile for thread-safe GUI reading.
 */
public class Vehicle {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private final int id;
    private final VehicleType type;
    private final Direction direction;
    private volatile VehicleStatus status;
    private volatile int x;
    private volatile int y;
    private final long creationTime;
    private volatile long waitStartTime;
    private volatile long waitEndTime;
    private volatile boolean deadlocked;
    private volatile boolean inConvoy;
    private volatile boolean exchanging;

    // Constants for vehicle dimensions
    public static final int WIDTH = 30;
    public static final int HEIGHT = 16;

    public Vehicle(VehicleType type, Direction direction, int startX, int startY) {
        this.id = ID_COUNTER.incrementAndGet();
        this.type = type;
        this.direction = direction;
        this.status = VehicleStatus.CREATED;
        this.x = startX;
        this.y = startY;
        this.creationTime = System.currentTimeMillis();
        this.waitStartTime = 0;
        this.waitEndTime = 0;
        this.deadlocked = false;
    }

    /**
     * Resets the global vehicle ID counter. Called when simulation resets.
     */
    public static void resetIdCounter() {
        ID_COUNTER.set(0);
    }

    // --- Getters ---
    public int getVehicleId() { return id; }
    public VehicleType getType() { return type; }
    public Direction getDirection() { return direction; }
    public VehicleStatus getStatus() { return status; }
    public int getX() { return x; }
    public int getY() { return y; }
    public long getCreationTime() { return creationTime; }
    public long getWaitStartTime() { return waitStartTime; }
    public long getWaitEndTime() { return waitEndTime; }
    public boolean isDeadlocked() { return deadlocked; }
    public boolean isInConvoy() { return inConvoy; }
    public boolean isExchanging() { return exchanging; }
    public boolean isEmergency() { return type.isEmergency(); }

    // --- Setters ---
    public void setStatus(VehicleStatus status) { this.status = status; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setDeadlocked(boolean deadlocked) { this.deadlocked = deadlocked; }
    public void setInConvoy(boolean inConvoy) { this.inConvoy = inConvoy; }
    public void setExchanging(boolean exchanging) { this.exchanging = exchanging; }

    public void startWaiting() {
        this.waitStartTime = System.currentTimeMillis();
        this.status = VehicleStatus.WAITING;
    }

    public void stopWaiting() {
        this.waitEndTime = System.currentTimeMillis();
    }

    /**
     * Returns how long this vehicle has been waiting in milliseconds.
     */
    public long getWaitTime() {
        if (waitStartTime == 0) return 0;
        if (waitEndTime > 0) return waitEndTime - waitStartTime;
        return System.currentTimeMillis() - waitStartTime;
    }

    /**
     * Moves the vehicle one step forward in its direction.
     * @param speed pixels to move per step
     */
    public void moveForward(int speed) {
        switch (direction) {
            case NORTH: y += speed; break; // From north, heading south (down)
            case SOUTH: y -= speed; break; // From south, heading north (up)
            case EAST:  x -= speed; break; // From east, heading west (left)
            case WEST:  x += speed; break; // From west, heading east (right)
        }
    }

    /**
     * Check if vehicle is off screen (past the visible area).
     */
    public boolean isOffScreen(int panelWidth, int panelHeight) {
        return x < -60 || x > panelWidth + 60 || y < -60 || y > panelHeight + 60;
    }

    @Override
    public String toString() {
        return type.getDisplayName() + "-" + id + " (" + direction.getDisplayName() + ")";
    }
}
