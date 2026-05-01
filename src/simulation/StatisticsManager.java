package simulation;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks all simulation statistics using thread-safe atomic variables.
 * Provides real-time data for the StatsPanel.
 */
public class StatisticsManager {

    private final AtomicInteger totalGenerated = new AtomicInteger(0);
    private final AtomicInteger vehiclesPassed = new AtomicInteger(0);
    private final AtomicInteger vehiclesWaiting = new AtomicInteger(0);
    private final AtomicInteger emergencyHandled = new AtomicInteger(0);
    private final AtomicLong totalWaitTime = new AtomicLong(0);
    private final AtomicLong maxWaitTime = new AtomicLong(0);
    private final AtomicInteger waitTimeCount = new AtomicInteger(0);

    public void recordVehicleGenerated() {
        totalGenerated.incrementAndGet();
    }

    public void recordVehiclePassed() {
        vehiclesPassed.incrementAndGet();
    }

    public void incrementWaiting() {
        vehiclesWaiting.incrementAndGet();
    }

    public void decrementWaiting() {
        vehiclesWaiting.updateAndGet(v -> Math.max(0, v - 1));
    }

    public void recordEmergencyHandled() {
        emergencyHandled.incrementAndGet();
    }

    public void recordWaitTime(long waitTimeMs) {
        totalWaitTime.addAndGet(waitTimeMs);
        waitTimeCount.incrementAndGet();
        maxWaitTime.updateAndGet(current -> Math.max(current, waitTimeMs));
    }

    // --- Getters ---
    public int getTotalGenerated() { return totalGenerated.get(); }
    public int getVehiclesPassed() { return vehiclesPassed.get(); }
    public int getVehiclesWaiting() { return vehiclesWaiting.get(); }
    public int getEmergencyHandled() { return emergencyHandled.get(); }

    public long getAverageWaitTime() {
        int count = waitTimeCount.get();
        if (count == 0) return 0;
        return totalWaitTime.get() / count;
    }

    public long getMaxWaitTime() { return maxWaitTime.get(); }

    /**
     * Resets all statistics to zero.
     */
    public void reset() {
        totalGenerated.set(0);
        vehiclesPassed.set(0);
        vehiclesWaiting.set(0);
        emergencyHandled.set(0);
        totalWaitTime.set(0);
        maxWaitTime.set(0);
        waitTimeCount.set(0);
    }
}
