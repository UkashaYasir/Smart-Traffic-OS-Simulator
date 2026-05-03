package sync;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import utils.Logger;

/**
 * OS Concept: CYCLICBARRIER
 * 
 * Synchronizes convoy movement - a group of vehicles that move together.
 * 
 * A CyclicBarrier allows a set of threads to all wait for each other
 * to reach a common barrier point. Unlike CountDownLatch, a CyclicBarrier
 * can be reused after the waiting threads are released.
 * 
 * In this simulation:
 * - When a convoy is formed (e.g., 3 vehicles from the same direction),
 *   all vehicles in the convoy must reach the intersection before any cross
 * - Each vehicle calls await() at the barrier
 * - When all convoy members arrive, they all proceed together
 * - The barrier resets automatically for the next convoy
 * 
 * This demonstrates group synchronization where threads must wait for peers.
 */
public class ConvoyBarrier {

    private CyclicBarrier barrier;
    private final Logger logger = Logger.getInstance();
    private int partyCount;

    /**
     * Creates a convoy barrier for the given number of vehicles.
     * @param parties number of vehicles in the convoy
     */
    public ConvoyBarrier(int parties) {
        this.partyCount = parties;
        this.barrier = new CyclicBarrier(parties, () -> {
            // This runs when all parties arrive at the barrier
            logger.log("═══ Convoy barrier tripped! All " + parties
                    + " vehicles proceed together. ═══");
        });
    }

    /**
     * Vehicle waits at the barrier until all convoy members arrive.
     * @param vehicleName name of the waiting vehicle
     */
    public void awaitConvoy(String vehicleName) throws InterruptedException, BrokenBarrierException {
        logger.log(vehicleName + " arrived at convoy barrier ("
                + (barrier.getNumberWaiting() + 1) + "/" + partyCount + ")");
        barrier.await();
    }

    /**
     * Resets the barrier for a new convoy.
     * Notifies any currently waiting threads that the barrier is being reset.
     */
    public void reset(int newParties) {
        if (this.barrier != null) {
            this.barrier.reset(); // Breaks the barrier and notifies waiting threads
        }
        this.partyCount = newParties;
        this.barrier = new CyclicBarrier(newParties, () -> {
            logger.log("═══ Convoy barrier tripped! All " + newParties
                    + " vehicles proceed together. ═══");
        });
    }

    /**
     * Returns how many threads are currently waiting at the barrier.
     */
    public int getWaitingCount() {
        return barrier.getNumberWaiting();
    }

    /**
     * Returns total number of parties needed.
     */
    public int getPartyCount() {
        return partyCount;
    }
}
