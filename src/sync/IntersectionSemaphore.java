package sync;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import model.Vehicle;
import utils.Logger;

/**
 * OS Concept: SEMAPHORE
 * 
 * Controls access to the intersection (the "Critical Section").
 * The intersection is a shared resource that can only allow a limited 
 * number of threads (vehicles) at a time to prevent collisions.
 * 
 * A Semaphore maintains a set of "permits." 
 * - acquire(): A thread takes a permit. If no permits are left, it blocks (waits).
 * - release(): A thread returns the permit, allowing a waiting thread to proceed.
 * 
 * Implementation Details:
 * - maxPermits: Limits concurrent access (degree of parallelism).
 * - fairness: Set to 'true' (FIFO) to prevent vehicle starvation.
 */
public class IntersectionSemaphore {

    private final Semaphore semaphore;
    private final Logger logger = Logger.getInstance();
    private final int maxPermits;

    /**
     * Creates an intersection semaphore with the given number of permits.
     * @param permits maximum vehicles allowed in intersection simultaneously
     */
    public IntersectionSemaphore(int permits) {
        this.maxPermits = permits;
        // fair=true means threads acquire permits in FIFO order
        this.semaphore = new Semaphore(permits, true);
    }

    /**
     * Vehicle tries to enter the intersection.
     * Blocks until a permit is available.
     */
    public void acquire(Vehicle vehicle) throws InterruptedException {
        logger.log(vehicle + " requesting semaphore (available: "
                + semaphore.availablePermits() + "/" + maxPermits + ")");
        semaphore.acquire();
        logger.log(vehicle + " acquired semaphore [OK]");
    }

    /**
     * Vehicle tries to enter with a timeout.
     * Returns true if permit was acquired, false if timed out.
     * Only logs on success to avoid log spam during retries.
     */
    public boolean tryAcquire(Vehicle vehicle, long timeoutMs) throws InterruptedException {
        boolean acquired = semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        if (acquired) {
            logger.log(vehicle + " acquired semaphore (available: "
                    + semaphore.availablePermits() + "/" + maxPermits + ") [OK]");
        }
        // Don't log on timeout — vehicle will retry silently
        return acquired;
    }

    /**
     * Vehicle leaves the intersection, releasing its permit.
     */
    public void release(Vehicle vehicle) {
        semaphore.release();
        logger.log(vehicle + " released semaphore (available: "
                + semaphore.availablePermits() + "/" + maxPermits + ")");
    }

    public int getAvailablePermits() {
        return semaphore.availablePermits();
    }

    public int getMaxPermits() {
        return maxPermits;
    }
}
