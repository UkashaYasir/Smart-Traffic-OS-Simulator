package sync;

import java.util.concurrent.CountDownLatch;
import utils.Logger;

/**
 * OS Concept: COUNTDOWNLATCH
 * 
 * Synchronizes the start of multiple threads using a "Starting Gun" pattern.
 * 
 * A CountDownLatch is a synchronization aid that allows one or more threads
 * to wait until a set of operations being performed in other threads completes.
 * 
 * Key Characteristics:
 * - One-time Use: The count cannot be reset once it reaches zero (must create new instance).
 * - Release: Once the latch is fired, it remains open and subsequent threads pass freely.
 * 
 * In this simulation:
 * - Ensures that no vehicle thread begins its logic until the engine is fully running.
 * - Simulates the coordinated release of resources or threads during initialization.
 */
public class StartLatch {

    private volatile CountDownLatch latch;
    private final Logger logger = Logger.getInstance();

    public StartLatch() {
        this.latch = new CountDownLatch(1);
    }

    /**
     * Vehicles call this to wait for the start signal.
     * All vehicles will be released at the same time.
     */
    public void awaitStart() throws InterruptedException {
        logger.log("Thread " + Thread.currentThread().getName() + " waiting at start latch...");
        latch.await();
    }

    /**
     * Fires the start signal, releasing all waiting vehicle threads.
     */
    public void fireStart() {
        logger.log("★ Start latch fired! All vehicles released simultaneously.");
        latch.countDown();
    }

    /**
     * Resets the latch for a new simulation round.
     * Creates a brand new CountDownLatch since they cannot be reused.
     */
    public void reset() {
        this.latch = new CountDownLatch(1);
        logger.log("Start latch reset for new round.");
    }

    /**
     * Returns the current count of the latch.
     */
    public long getCount() {
        return latch.getCount();
    }
}
