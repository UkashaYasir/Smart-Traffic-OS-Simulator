package simulation;

import model.*;
import sync.*;
import ai.SmartTrafficController;
import utils.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central simulation engine that coordinates all components.
 * Manages vehicle lifecycle, traffic light cycling, and synchronization.
 * 
 * This class ties together all OS concepts:
 * - Semaphore: controls intersection access
 * - CountDownLatch (StartLatch): synchronized start
 * - CyclicBarrier (ConvoyBarrier): convoy movement
 * - Phaser (TrafficPhaser): signal phase control
 * - Exchanger (VehicleExchanger): vehicle data exchange
 * - DeadlockManager: deadlock demo
 */
public class TrafficSimulationEngine {

    // --- Components ---
    private final Map<Direction, Lane> lanes;
    private final IntersectionSemaphore semaphore;
    private final StartLatch startLatch;
    private final ConvoyBarrier convoyBarrier;
    private final TrafficPhaser trafficPhaser;
    private final VehicleExchanger vehicleExchanger;
    private final DeadlockManager deadlockManager;
    private final SmartTrafficController aiController;
    private final StatisticsManager stats;
    private final VehicleGenerator vehicleGenerator;
    private final Logger logger = Logger.getInstance();

    // --- Vehicle tracking ---
    private final CopyOnWriteArrayList<Vehicle> allVehicles = new CopyOnWriteArrayList<>();

    // --- Simulation state ---
    private volatile boolean running = false;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    // --- Threads ---
    private Thread signalThread;
    private Thread generatorThread;
    private Thread aiThread;
    private final List<Thread> vehicleThreads = Collections.synchronizedList(new ArrayList<>());

    // --- Layout constants (must match IntersectionPanel) ---
    public static final int PANEL_SIZE = 700;
    public static final int ROAD_WIDTH = 120;
    public static final int STOP_LINE_NS_TOP = (PANEL_SIZE - ROAD_WIDTH) / 2 - Vehicle.HEIGHT;
    public static final int STOP_LINE_NS_BOTTOM = (PANEL_SIZE + ROAD_WIDTH) / 2;
    public static final int STOP_LINE_EW_LEFT = (PANEL_SIZE - ROAD_WIDTH) / 2 - Vehicle.WIDTH;
    public static final int STOP_LINE_EW_RIGHT = (PANEL_SIZE + ROAD_WIDTH) / 2;

    // --- Movement constants ---
    private static final int VEHICLE_SPEED = 2;
    private static final int INTERSECTION_SPEED = 3;  // Faster through intersection
    private static final int EXIT_SPEED = 4;           // Fast exit off screen
    private static final int MOVE_DELAY = 20;          // ms between moves
    private static final int MIN_SPACING = 42;         // Min gap between same-lane vehicles
    private static final int MAX_VEHICLES = 16;        // Max vehicles on screen

    /**
     * Listener interface for GUI updates.
     */
    public interface SimulationListener {
        void onSimulationUpdate();
    }

    private SimulationListener listener;

    public TrafficSimulationEngine() {
        // Initialize lanes
        lanes = new LinkedHashMap<>();
        for (Direction dir : Direction.values()) {
            lanes.put(dir, new Lane(dir));
        }

        // Initialize synchronization primitives
        semaphore = new IntersectionSemaphore(3);    // Allow 3 vehicles at once
        startLatch = new StartLatch();
        convoyBarrier = new ConvoyBarrier(3);
        trafficPhaser = new TrafficPhaser();
        vehicleExchanger = new VehicleExchanger();
        deadlockManager = new DeadlockManager();

        // Initialize AI and stats
        aiController = new SmartTrafficController();
        stats = new StatisticsManager();
        vehicleGenerator = new VehicleGenerator(lanes, stats);

        // Set initial traffic lights: N-S green, E-W red
        lanes.get(Direction.NORTH).setLight(TrafficLight.GREEN);
        lanes.get(Direction.SOUTH).setLight(TrafficLight.GREEN);
        lanes.get(Direction.EAST).setLight(TrafficLight.RED);
        lanes.get(Direction.WEST).setLight(TrafficLight.RED);
    }

    public void setListener(SimulationListener listener) {
        this.listener = listener;
    }

    // ==========================================
    // Simulation Control Methods
    // ==========================================

    /**
     * Starts the simulation.
     */
    public void start() {
        if (running) return;
        running = true;
        paused = false;

        logger.log("====================================");
        logger.log(">> SIMULATION STARTED");
        logger.log("====================================");

        // Fire the start latch to release any waiting vehicles
        startLatch.fireStart();

        // Start the traffic signal cycling thread
        startSignalThread();

        // Start the vehicle generator thread
        startGeneratorThread();

        // Start the AI controller thread
        startAIThread();
    }

    /**
     * Pauses the simulation.
     */
    public void pause() {
        if (!running || paused) return;
        paused = true;
        logger.log("|| SIMULATION PAUSED");
    }

    /**
     * Resumes the simulation.
     */
    public void resume() {
        if (!running || !paused) return;
        paused = false;
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
        logger.log(">> SIMULATION RESUMED");
    }

    /**
     * Stops the simulation and cleans up all threads.
     */
    public void stop() {
        if (!running) return;
        running = false;
        paused = false;

        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }

        // Interrupt all threads
        if (signalThread != null) signalThread.interrupt();
        if (generatorThread != null) generatorThread.interrupt();
        if (aiThread != null) aiThread.interrupt();

        synchronized (vehicleThreads) {
            for (Thread t : vehicleThreads) {
                t.interrupt();
            }
            vehicleThreads.clear();
        }

        logger.log("[] SIMULATION STOPPED");
    }

    /**
     * Resets the entire simulation to initial state.
     */
    public void reset() {
        stop();

        try { Thread.sleep(300); } catch (InterruptedException e) { /* ok */ }

        allVehicles.clear();
        for (Lane lane : lanes.values()) {
            lane.clear();
        }

        Vehicle.resetIdCounter();
        stats.reset();
        startLatch.reset();
        trafficPhaser.reset();
        deadlockManager.reset();
        logger.clear();

        // Reset traffic lights
        lanes.get(Direction.NORTH).setLight(TrafficLight.GREEN);
        lanes.get(Direction.SOUTH).setLight(TrafficLight.GREEN);
        lanes.get(Direction.EAST).setLight(TrafficLight.RED);
        lanes.get(Direction.WEST).setLight(TrafficLight.RED);

        logger.log("SIMULATION RESET -- Ready to start");
    }

    // ==========================================
    // Vehicle Management
    // ==========================================

    /**
     * Adds a normal vehicle to a random lane.
     */
    public void addNormalVehicle() {
        if (allVehicles.size() >= MAX_VEHICLES) {
            logger.log("Max vehicles reached. Wait for some to pass.");
            return;
        }
        Vehicle vehicle = vehicleGenerator.generateVehicle(VehicleType.NORMAL);
        allVehicles.add(vehicle);
        startVehicleThread(vehicle);
    }

    /**
     * Adds an emergency vehicle to a random lane.
     */
    public void addEmergencyVehicle() {
        VehicleType type = Math.random() > 0.5 ? VehicleType.AMBULANCE : VehicleType.POLICE;
        Vehicle vehicle = vehicleGenerator.generateVehicle(type);
        allVehicles.add(vehicle);
        startVehicleThread(vehicle);
        stats.recordEmergencyHandled();
        logger.log("!! Emergency vehicle priority activated for " + vehicle);
    }

    /**
     * Starts a thread for a single vehicle.
     * This is the core of the project where vehicles move through their lifecycle.
     * Each vehicle follows a "Critical Section" pattern:
     * 1. PRE-SYNC (Wait for StartLatch)
     * 2. ENTRY SECTION (Approach & request Semaphore)
     * 3. CRITICAL SECTION (Crossing the intersection)
     * 4. EXIT SECTION (Release Semaphore & leave)
     */
    private void startVehicleThread(Vehicle vehicle) {
        Thread thread = new Thread(() -> {
            boolean semaphoreHeld = false;
            try {
                // PHASE 0: WAIT FOR ENGINE START (CountDownLatch)
                // Prevents vehicles from moving until simulation is fully initialized
                startLatch.awaitStart();

                vehicle.setStatus(VehicleStatus.APPROACHING);

                // =============================================
                // PHASE 1: APPROACH — Move toward stop line
                // Check for vehicles ahead to maintain spacing
                // =============================================
                while (running && !Thread.interrupted()) {
                    checkPaused();

                    // Check if we reached the stop line
                    if (isAtStopLine(vehicle)) break;

                    // Check if a vehicle ahead is too close — maintain spacing
                    if (isVehicleAhead(vehicle, MIN_SPACING)) {
                        Thread.sleep(MOVE_DELAY);
                        continue; // Don't move, wait for gap
                    }

                    vehicle.moveForward(VEHICLE_SPEED);
                    Thread.sleep(MOVE_DELAY);
                }

                if (!running || Thread.interrupted()) return;

                // =============================================
                // PHASE 2: WAIT — Wait at stop line
                // Must wait for green light AND semaphore permit
                // Uses tryAcquire so vehicle re-checks the light
                // =============================================
                vehicle.startWaiting();
                stats.incrementWaiting();
                logger.log(vehicle + " waiting at stop line");

                boolean entered = false;
                while (running && !Thread.interrupted() && !entered) {
                    checkPaused();

                    Lane lane = lanes.get(vehicle.getDirection());

                    // Wait until light is green
                    if (lane.getLight() != TrafficLight.GREEN) {
                        Thread.sleep(80);
                        continue;
                    }

                    // Also check spacing — don't enter if a vehicle is
                    // still inside the intersection ahead of us
                    if (isVehicleAhead(vehicle, MIN_SPACING)) {
                        Thread.sleep(50);
                        continue;
                    }

                    // --- NEW: DEADLOCK EFFECTS THE SIMULATION ---
                    // If a deadlock is active, NO vehicles can enter the intersection
                    if (deadlockManager.isDeadlockActive()) {
                        Thread.sleep(100);
                        continue;
                    }

                    // PHASE 1.5: CONVOY SYNC (CyclicBarrier)
                    // If vehicle is part of a convoy, it must wait for all members
                    // at the stop line before any are allowed to request the semaphore.
                    if (vehicle.isInConvoy()) {
                        vehicle.setStatus(VehicleStatus.WAITING);
                        try {
                            convoyBarrier.awaitConvoy(vehicle.toString());
                        } catch (Exception e) {
                            // Barrier reset or broken, proceed normally
                        }
                        vehicle.setStatus(VehicleStatus.APPROACHING);
                        // Brief pause to show they've been released
                        Thread.sleep(200);
                    }

                    // Light is green! Try to get semaphore (with timeout)
                    // If timeout, we loop back and re-check the light
                    entered = semaphore.tryAcquire(vehicle, 400);
                    if (!entered) {
                        Thread.sleep(50);
                    }
                }

                if (!running || Thread.interrupted()) return;

                // =============================================
                // PHASE 3: CROSS — Move through intersection
                // Semaphore is held. Drive faster through.
                // =============================================
                semaphoreHeld = true;
                vehicle.setStatus(VehicleStatus.IN_INTERSECTION);
                vehicle.stopWaiting();
                stats.decrementWaiting();
                stats.recordWaitTime(vehicle.getWaitTime());
                logger.log("[OK] " + vehicle + " ENTERED critical section");

                while (running && !Thread.interrupted()) {
                    checkPaused();
                    if (isPastIntersection(vehicle)) break;
                    vehicle.moveForward(INTERSECTION_SPEED);
                    Thread.sleep(MOVE_DELAY);
                }

                // Release semaphore — exit critical section
                semaphore.release(vehicle);
                semaphoreHeld = false;
                vehicle.setStatus(VehicleStatus.PASSED);
                stats.recordVehiclePassed();
                logger.log("[OK] " + vehicle + " PASSED intersection");

                // =============================================
                // PHASE 4: EXIT — Move off screen quickly
                // =============================================
                while (running && !Thread.interrupted()) {
                    if (vehicle.isOffScreen(PANEL_SIZE, PANEL_SIZE)) break;
                    vehicle.moveForward(EXIT_SPEED);
                    Thread.sleep(MOVE_DELAY);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // Safety: release semaphore if we still hold it
                if (semaphoreHeld) {
                    try { semaphore.release(vehicle); } catch (Exception e) { /* ok */ }
                }
                // Always clean up vehicle from lane and list
                lanes.get(vehicle.getDirection()).removeVehicle(vehicle);
                allVehicles.remove(vehicle);
                // Ensure waiting count stays correct
                if (vehicle.getStatus() == VehicleStatus.WAITING) {
                    stats.decrementWaiting();
                }
            }
        }, "Vehicle-" + vehicle.getVehicleId());

        thread.setDaemon(true);
        vehicleThreads.add(thread);
        thread.start();
    }

    // ==========================================
    // Vehicle Spacing & Position Checks
    // ==========================================

    /**
     * Checks if another vehicle in the SAME lane is directly ahead
     * and within the minimum distance. Prevents vehicles from overlapping.
     */
    private boolean isVehicleAhead(Vehicle v, int minDistance) {
        Direction dir = v.getDirection();

        for (Vehicle other : allVehicles) {
            // Skip self and vehicles in different lanes
            if (other == v || other.getDirection() != dir) continue;
            // Skip vehicles already passed
            if (other.getStatus() == VehicleStatus.PASSED) continue;

            switch (dir) {
                case NORTH: // Moving south (y increasing) — ahead = higher y
                    if (other.getY() > v.getY()
                            && other.getY() - v.getY() < minDistance)
                        return true;
                    break;
                case SOUTH: // Moving north (y decreasing) — ahead = lower y
                    if (other.getY() < v.getY()
                            && v.getY() - other.getY() < minDistance)
                        return true;
                    break;
                case EAST:  // Moving west (x decreasing) — ahead = lower x
                    if (other.getX() < v.getX()
                            && v.getX() - other.getX() < minDistance)
                        return true;
                    break;
                case WEST:  // Moving east (x increasing) — ahead = higher x
                    if (other.getX() > v.getX()
                            && other.getX() - v.getX() < minDistance)
                        return true;
                    break;
            }
        }
        return false;
    }

    /**
     * Checks if simulation is paused and waits if so.
     */
    private void checkPaused() throws InterruptedException {
        if (paused) {
            synchronized (pauseLock) {
                while (paused) {
                    pauseLock.wait();
                }
            }
        }
    }

    /**
     * Checks if vehicle has reached its stop line.
     */
    private boolean isAtStopLine(Vehicle v) {
        switch (v.getDirection()) {
            case NORTH: return v.getY() >= STOP_LINE_NS_TOP;
            case SOUTH: return v.getY() <= STOP_LINE_NS_BOTTOM;
            case WEST:  return v.getX() >= STOP_LINE_EW_LEFT;
            case EAST:  return v.getX() <= STOP_LINE_EW_RIGHT;
            default: return false;
        }
    }

    /**
     * Checks if vehicle has passed through the intersection.
     */
    private boolean isPastIntersection(Vehicle v) {
        int past = 50; // pixels past intersection
        switch (v.getDirection()) {
            case NORTH: return v.getY() >= STOP_LINE_NS_BOTTOM + past;
            case SOUTH: return v.getY() <= STOP_LINE_NS_TOP - past;
            case WEST:  return v.getX() >= STOP_LINE_EW_RIGHT + past;
            case EAST:  return v.getX() <= STOP_LINE_EW_LEFT - past;
            default: return false;
        }
    }

    // ==========================================
    // Background Threads
    // ==========================================

    /**
     * Signal thread: cycles through traffic light phases using the Phaser.
     */
    private void startSignalThread() {
        signalThread = new Thread(() -> {
            try {
                while (running && !Thread.interrupted()) {
                    checkPaused();

                    // Wait for current phase duration
                    int duration = trafficPhaser.getCurrentPhaseDuration();
                    Thread.sleep(duration);

                    if (!running) break;

                    // Advance to next phase
                    trafficPhaser.advancePhase(lanes);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "SignalController");
        signalThread.setDaemon(true);
        signalThread.start();
    }

    /**
     * Generator thread: creates random vehicles periodically.
     */
    private void startGeneratorThread() {
        generatorThread = new Thread(() -> {
            Random random = new Random();
            try {
                while (running && !Thread.interrupted()) {
                    checkPaused();

                    // Generate a vehicle every 2.5-5 seconds
                    Thread.sleep(2500 + random.nextInt(2500));

                    if (!running) break;

                    // Don't generate too many vehicles
                    if (allVehicles.size() < MAX_VEHICLES) {
                        Vehicle vehicle = vehicleGenerator.generateRandomVehicle();
                        allVehicles.add(vehicle);
                        startVehicleThread(vehicle);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "VehicleGenerator");
        generatorThread.setDaemon(true);
        generatorThread.start();
    }

    /**
     * AI thread: periodically analyzes traffic and adjusts timing.
     */
    private void startAIThread() {
        aiThread = new Thread(() -> {
            try {
                while (running && !Thread.interrupted()) {
                    checkPaused();
                    Thread.sleep(3000); // Analyze every 3 seconds

                    if (!running) break;

                    aiController.analyze(lanes, trafficPhaser);
                    aiController.assessDeadlockRisk(lanes);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "AIController");
        aiThread.setDaemon(true);
        aiThread.start();
    }

    // ==========================================
    // Synchronization Demos
    // ==========================================

    /**
     * Triggers the deadlock demonstration.
     */
    public void triggerDeadlock() {
        deadlockManager.triggerDeadlock();
    }

    /**
     * Resolves the current deadlock.
     */
    public void resolveDeadlock() {
        deadlockManager.resolveDeadlock();
    }

    /**
     * Demonstrates the Exchanger by having two vehicles swap data.
     */
    public void demonstrateExchanger() {
        if (!running) {
            logger.log("⚠ Start the simulation before running the Exchanger demo.");
            return;
        }
        int nsCount = lanes.get(Direction.NORTH).getVehicleCount();
        int ewCount = lanes.get(Direction.EAST).getVehicleCount();
        
        // The exchanger demo creates threads. We should track them.
        Thread[] threads = vehicleExchanger.createDemonstrationThreads(
            "NorthVehicle", "North congestion: " + nsCount + " vehicles",
            "EastVehicle", "East congestion: " + ewCount + " vehicles"
        );
        
        for (Thread t : threads) {
            vehicleThreads.add(t);
            t.start();
        }
    }

    /**
     * Demonstrates the ConvoyBarrier.
     */
    public void demonstrateConvoy() {
        if (!running) {
            logger.log("⚠ Start the simulation before running the Convoy demo.");
            return;
        }
        
        int convoySize = 3;
        convoyBarrier.reset(convoySize);
        logger.log("=== Visual Convoy Demo: " + convoySize + " Purple vehicles will sync at North lane ===");

        for (int i = 0; i < convoySize; i++) {
            final int offset = i;
            new Thread(() -> {
                try {
                    Thread.sleep(offset * 800); // Stagger their approach
                    
                    // Create a REAL vehicle in the North lane
                    Vehicle v = new Vehicle(VehicleType.NORMAL, Direction.NORTH, 365, -50);
                    v.setInConvoy(true); // Mark for special handling
                    
                    allVehicles.add(v);
                    startVehicleThread(v);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    // ==========================================
    // Getters
    // ==========================================

    public List<Vehicle> getAllVehicles() { return new ArrayList<>(allVehicles); }
    public Map<Direction, Lane> getLanes() { return lanes; }
    public StatisticsManager getStats() { return stats; }
    public TrafficPhaser getTrafficPhaser() { return trafficPhaser; }
    public DeadlockManager getDeadlockManager() { return deadlockManager; }
    public SmartTrafficController getAIController() { return aiController; }
    public IntersectionSemaphore getSemaphore() { return semaphore; }
    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }
}
