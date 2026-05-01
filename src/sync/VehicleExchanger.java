package sync;

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import utils.Logger;

/**
 * OS Concept: EXCHANGER
 * 
 * Allows two vehicles to exchange route/congestion information.
 * 
 * An Exchanger is a synchronization point where two threads can swap data.
 * Each thread presents some object on entry and receives the object from
 * the partner thread on return.
 * 
 * In this simulation:
 * - Two vehicles approaching from opposite directions can exchange info
 * - They share congestion data (how many vehicles are waiting behind them)
 * - This information helps the AI controller make better decisions
 * 
 * This demonstrates inter-thread communication and data exchange.
 */
public class VehicleExchanger {

    private final Exchanger<String> exchanger;
    private final Logger logger = Logger.getInstance();

    /**
     * Data class to hold exchangeable information.
     */
    public static class CongestionInfo {
        public final String vehicleName;
        public final String direction;
        public final int queueLength;
        public final long waitTime;

        public CongestionInfo(String vehicleName, String direction,
                              int queueLength, long waitTime) {
            this.vehicleName = vehicleName;
            this.direction = direction;
            this.queueLength = queueLength;
            this.waitTime = waitTime;
        }

        @Override
        public String toString() {
            return direction + ": queue=" + queueLength + ", wait=" + waitTime + "ms";
        }
    }

    public VehicleExchanger() {
        this.exchanger = new Exchanger<>();
    }

    /**
     * Exchanges congestion information between two vehicles.
     * Blocks until the partner thread arrives or timeout occurs.
     * 
     * @param myInfo information from this vehicle
     * @param timeoutMs maximum time to wait for exchange partner
     * @return information from the partner vehicle, or null if timed out
     */
    public String exchange(String myInfo, long timeoutMs) {
        try {
            logger.log("Exchange offered: " + myInfo);
            String partnerInfo = exchanger.exchange(myInfo, timeoutMs, TimeUnit.MILLISECONDS);
            logger.log("Exchange completed! Received: " + partnerInfo);
            return partnerInfo;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Exchange interrupted.");
            return null;
        } catch (TimeoutException e) {
            logger.log("Exchange timed out - no partner available.");
            return null;
        }
    }

    /**
     * Demonstrates the exchange between two vehicle threads.
     * Creates two threads that swap congestion data.
     */
    public void demonstrateExchange(String vehicle1Name, String vehicle1Info,
                                     String vehicle2Name, String vehicle2Info) {
        Thread t1 = new Thread(() -> {
            try {
                logger.log(vehicle1Name + " offering: " + vehicle1Info);
                String received = exchanger.exchange(vehicle1Info, 5000, TimeUnit.MILLISECONDS);
                logger.log(vehicle1Name + " received from partner: " + received);
            } catch (Exception e) {
                logger.log(vehicle1Name + " exchange failed: " + e.getMessage());
            }
        }, "Exchanger-" + vehicle1Name);

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(200); // Small delay to show both threads
                logger.log(vehicle2Name + " offering: " + vehicle2Info);
                String received = exchanger.exchange(vehicle2Info, 5000, TimeUnit.MILLISECONDS);
                logger.log(vehicle2Name + " received from partner: " + received);
            } catch (Exception e) {
                logger.log(vehicle2Name + " exchange failed: " + e.getMessage());
            }
        }, "Exchanger-" + vehicle2Name);

        t1.start();
        t2.start();
    }
}
