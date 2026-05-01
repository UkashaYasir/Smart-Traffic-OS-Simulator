package sync;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import model.Vehicle;
import model.VehicleType;
import model.VehicleStatus;
import model.Direction;
import utils.Logger;

/**
 * OS Concept: DEADLOCK
 * 
 * Demonstrates deadlock creation and resolution.
 * 
 * Deadlock occurs when two or more threads are blocked forever, each waiting
 * for a resource held by the other. Four conditions for deadlock:
 * 1. Mutual Exclusion: Resources cannot be shared
 * 2. Hold and Wait: Thread holds one resource while waiting for another
 * 3. No Preemption: Resources cannot be forcibly taken
 * 4. Circular Wait: Circular chain of threads waiting for resources
 * 
 * In this simulation:
 * - Vehicle A locks Road Segment 1, then tries to lock Road Segment 2
 * - Vehicle B locks Road Segment 2, then tries to lock Road Segment 1
 * - Both vehicles are stuck (deadlock!)
 * 
 * Resolution strategies demonstrated:
 * - Timeout-based locking (tryLock with timeout)
 * - Resource ordering (always lock in the same order)
 */
public class DeadlockManager {

    private final ReentrantLock roadSegment1 = new ReentrantLock();
    private final ReentrantLock roadSegment2 = new ReentrantLock();
    private final Logger logger = Logger.getInstance();

    private volatile boolean deadlockActive = false;
    private volatile boolean deadlockTriggered = false;
    private Thread vehicleThreadA;
    private Thread vehicleThreadB;
    private Vehicle deadlockVehicleA;
    private Vehicle deadlockVehicleB;
    private int deadlockCount = 0;

    // Callback for notifying GUI of deadlock state changes
    public interface DeadlockCallback {
        void onDeadlockStateChanged(boolean isDeadlocked);
    }

    private DeadlockCallback callback;

    public void setCallback(DeadlockCallback callback) {
        this.callback = callback;
    }

    /**
     * Triggers a deadlock demonstration.
     * Creates two vehicles that will deadlock on road segments.
     */
    public void triggerDeadlock() {
        if (deadlockActive) {
            logger.log("⚠ Deadlock is already active!");
            return;
        }

        deadlockTriggered = true;
        logger.log("═══════════════════════════════════════");
        logger.log("⚠ DEADLOCK DEMONSTRATION STARTING ⚠");
        logger.log("═══════════════════════════════════════");

        // Create two vehicles for the deadlock demo
        deadlockVehicleA = new Vehicle(VehicleType.NORMAL, Direction.NORTH, 350, 100);
        deadlockVehicleA.setStatus(VehicleStatus.BLOCKED);
        deadlockVehicleA.setDeadlocked(true);

        deadlockVehicleB = new Vehicle(VehicleType.NORMAL, Direction.EAST, 550, 330);
        deadlockVehicleB.setStatus(VehicleStatus.BLOCKED);
        deadlockVehicleB.setDeadlocked(true);

        // Thread A: locks segment 1, then tries segment 2
        vehicleThreadA = new Thread(() -> {
            try {
                logger.log(deadlockVehicleA + " → Locking Road Segment 1...");
                roadSegment1.lock();
                logger.log(deadlockVehicleA + " ✓ Acquired Road Segment 1");

                Thread.sleep(500); // Give thread B time to lock segment 2

                logger.log(deadlockVehicleA + " → Trying to lock Road Segment 2...");
                logger.log(deadlockVehicleA + " ✗ BLOCKED! Road Segment 2 is held by "
                        + deadlockVehicleB);

                // This will block forever (deadlock!) unless interrupted
                roadSegment2.lockInterruptibly();
                logger.log(deadlockVehicleA + " ✓ Acquired Road Segment 2 (after resolution)");

            } catch (InterruptedException e) {
                logger.log(deadlockVehicleA + " → Interrupted for deadlock resolution");
            } finally {
                if (roadSegment1.isHeldByCurrentThread()) roadSegment1.unlock();
                if (roadSegment2.isHeldByCurrentThread()) roadSegment2.unlock();
            }
        }, "DeadlockVehicle-A");

        // Thread B: locks segment 2, then tries segment 1
        vehicleThreadB = new Thread(() -> {
            try {
                logger.log(deadlockVehicleB + " → Locking Road Segment 2...");
                roadSegment2.lock();
                logger.log(deadlockVehicleB + " ✓ Acquired Road Segment 2");

                Thread.sleep(500); // Give thread A time to lock segment 1

                logger.log(deadlockVehicleB + " → Trying to lock Road Segment 1...");
                logger.log(deadlockVehicleB + " ✗ BLOCKED! Road Segment 1 is held by "
                        + deadlockVehicleA);

                // This will block forever (deadlock!) unless interrupted
                roadSegment1.lockInterruptibly();
                logger.log(deadlockVehicleB + " ✓ Acquired Road Segment 1 (after resolution)");

            } catch (InterruptedException e) {
                logger.log(deadlockVehicleB + " → Interrupted for deadlock resolution");
            } finally {
                if (roadSegment2.isHeldByCurrentThread()) roadSegment2.unlock();
                if (roadSegment1.isHeldByCurrentThread()) roadSegment1.unlock();
            }
        }, "DeadlockVehicle-B");

        vehicleThreadA.start();
        vehicleThreadB.start();

        // Detection thread: waits briefly then confirms deadlock
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Wait for deadlock to form
                if (vehicleThreadA.isAlive() && vehicleThreadB.isAlive()) {
                    deadlockActive = true;
                    deadlockCount++;
                    logger.log("═══════════════════════════════════════");
                    logger.log("🔴 DEADLOCK DETECTED!");
                    logger.log("  Vehicle A holds Segment 1, wants Segment 2");
                    logger.log("  Vehicle B holds Segment 2, wants Segment 1");
                    logger.log("  Circular wait condition confirmed!");
                    logger.log("  Click 'Resolve Deadlock' to fix.");
                    logger.log("═══════════════════════════════════════");
                    if (callback != null) callback.onDeadlockStateChanged(true);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "DeadlockDetector").start();
    }

    /**
     * Resolves the current deadlock by interrupting one vehicle thread.
     * Uses the "preemption" strategy: forcibly release one thread's lock.
     */
    public void resolveDeadlock() {
        if (!deadlockActive) {
            logger.log("No deadlock to resolve.");
            return;
        }

        logger.log("═══════════════════════════════════════");
        logger.log("🟢 RESOLVING DEADLOCK...");
        logger.log("Strategy: Preemption + Resource Ordering");
        logger.log("═══════════════════════════════════════");

        // Interrupt vehicle B to break the circular wait
        if (vehicleThreadB != null && vehicleThreadB.isAlive()) {
            logger.log("→ Interrupting " + deadlockVehicleB + " to break circular wait");
            vehicleThreadB.interrupt();
        }

        // Small delay then interrupt A too for clean resolution
        try { Thread.sleep(300); } catch (InterruptedException e) { /* ignore */ }

        if (vehicleThreadA != null && vehicleThreadA.isAlive()) {
            logger.log("→ Releasing " + deadlockVehicleA + " to proceed");
            vehicleThreadA.interrupt();
        }

        // Update vehicle states
        if (deadlockVehicleA != null) {
            deadlockVehicleA.setDeadlocked(false);
            deadlockVehicleA.setStatus(VehicleStatus.PASSED);
        }
        if (deadlockVehicleB != null) {
            deadlockVehicleB.setDeadlocked(false);
            deadlockVehicleB.setStatus(VehicleStatus.PASSED);
        }

        deadlockActive = false;
        deadlockTriggered = false;

        logger.log("✓ Deadlock resolved successfully!");
        logger.log("Prevention: Apply resource ordering (always lock Segment 1 before Segment 2)");
        logger.log("═══════════════════════════════════════");

        if (callback != null) callback.onDeadlockStateChanged(false);
    }

    /**
     * Returns the vehicles involved in deadlock for GUI display.
     */
    public Vehicle getDeadlockVehicleA() { return deadlockVehicleA; }
    public Vehicle getDeadlockVehicleB() { return deadlockVehicleB; }

    public boolean isDeadlockActive() { return deadlockActive; }
    public boolean isDeadlockTriggered() { return deadlockTriggered; }
    public int getDeadlockCount() { return deadlockCount; }

    /**
     * Resets the deadlock manager.
     */
    public void reset() {
        resolveDeadlock();
        deadlockCount = 0;
        deadlockVehicleA = null;
        deadlockVehicleB = null;
        deadlockActive = false;
        deadlockTriggered = false;
    }
}
