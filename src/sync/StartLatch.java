package sync;

import java.util.concurrent.CountDownLatch;
import utils.Logger;

/**
 * OS Concept: COUNTDOWNLATCH
 * 
 * Ensures all vehicle threads start moving at the same time.
 * 
 * A CountDownLatch is initialized with a count. Threads call await() to wait,
 * and the latch releases all waiting threads when the count reaches zero
 * through countDown() calls.
 * 
 * In this simulation:
 * - When the simulation starts, a latch is created with count = 1
 * - All vehicle threads call await() after being created
 * - When the user clicks "Start", countDown() is called
 * - All vehicles begin moving simultaneously
 * 
 * This demonstrates the "starting gun" synchronization pattern.
 * Unlike a CyclicBarrier, a CountDownLatch can only be used ONCE.
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
