package simulation;

import model.*;
import utils.Logger;
import java.util.Map;
import java.util.Random;

/**
 * Generates vehicles at random intervals and adds them to lanes.
 * Runs on a background thread during the simulation.
 */
public class VehicleGenerator {

    private final Logger logger = Logger.getInstance();
    private final Random random = new Random();
    private final Map<Direction, Lane> lanes;
    private final StatisticsManager stats;

    // Panel layout constants (must match IntersectionPanel)
    public static final int PANEL_SIZE = 700;
    public static final int ROAD_WIDTH = 120;
    public static final int INTERSECTION_LEFT = (PANEL_SIZE - ROAD_WIDTH) / 2;   // 290
    public static final int INTERSECTION_RIGHT = (PANEL_SIZE + ROAD_WIDTH) / 2;  // 410
    public static final int INTERSECTION_TOP = (PANEL_SIZE - ROAD_WIDTH) / 2;    // 290
    public static final int INTERSECTION_BOTTOM = (PANEL_SIZE + ROAD_WIDTH) / 2; // 410

    public VehicleGenerator(Map<Direction, Lane> lanes, StatisticsManager stats) {
        this.lanes = lanes;
        this.stats = stats;
    }

    /**
     * Creates a random vehicle and adds it to a random lane.
     */
    public Vehicle generateRandomVehicle() {
        Direction dir = Direction.values()[random.nextInt(4)];
        VehicleType type = randomVehicleType();
        return createVehicle(type, dir);
    }

    /**
     * Creates a vehicle of the specified type in a random direction.
     */
    public Vehicle generateVehicle(VehicleType type) {
        Direction dir = Direction.values()[random.nextInt(4)];
        return createVehicle(type, dir);
    }

    /**
     * Creates a vehicle in a specific direction.
     */
    public Vehicle generateVehicle(VehicleType type, Direction dir) {
        return createVehicle(type, dir);
    }

    /**
     * Creates a vehicle and adds it to the appropriate lane.
     */
    private Vehicle createVehicle(VehicleType type, Direction dir) {
        // Calculate starting position based on direction
        int startX, startY;
        Lane lane = lanes.get(dir);
        int queueOffset = lane.getVehicleCount() * 40; // Space between queued vehicles

        switch (dir) {
            case NORTH:
                // Vehicles from north: right half of vertical road, starting above
                startX = INTERSECTION_LEFT + ROAD_WIDTH / 2 + 15; // ~365
                startY = -20 - queueOffset;
                break;
            case SOUTH:
                // Vehicles from south: left half of vertical road, starting below
                startX = INTERSECTION_LEFT + ROAD_WIDTH / 2 - 45; // ~305
                startY = PANEL_SIZE + 20 + queueOffset;
                break;
            case EAST:
                // Vehicles from east: top half of horizontal road, starting right
                startX = PANEL_SIZE + 20 + queueOffset;
                startY = INTERSECTION_TOP + ROAD_WIDTH / 2 - 45; // ~305
                break;
            case WEST:
                // Vehicles from west: bottom half of horizontal road, starting left
                startX = -20 - queueOffset;
                startY = INTERSECTION_TOP + ROAD_WIDTH / 2 + 15; // ~365
                break;
            default:
                startX = 0; startY = 0;
        }

        Vehicle vehicle = new Vehicle(type, dir, startX, startY);
        lane.addVehicle(vehicle);
        stats.recordVehicleGenerated();

        logger.log("🚗 " + vehicle + " created at " + dir.getDisplayName() + " lane");
        return vehicle;
    }

    /**
     * Returns a random vehicle type with weighted probabilities.
     * Normal: 70%, Bus: 15%, Ambulance: 10%, Police: 5%
     */
    private VehicleType randomVehicleType() {
        int roll = random.nextInt(100);
        if (roll < 70) return VehicleType.NORMAL;
        if (roll < 85) return VehicleType.BUS;
        if (roll < 95) return VehicleType.AMBULANCE;
        return VehicleType.POLICE;
    }
}
