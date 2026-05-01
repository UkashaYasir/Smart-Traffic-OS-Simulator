package model;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a traffic lane for one direction of the intersection.
 * Contains a thread-safe queue of vehicles and the current traffic light state.
 * 
 * OS Concept: Uses ConcurrentLinkedQueue for thread-safe access by
 * multiple vehicle threads and the simulation engine simultaneously.
 */
public class Lane {

    private final Direction direction;
    private final ConcurrentLinkedQueue<Vehicle> vehicles;
    private volatile TrafficLight light;

    public Lane(Direction direction) {
        this.direction = direction;
        this.vehicles = new ConcurrentLinkedQueue<>();
        this.light = TrafficLight.RED; // Start with red
    }

    /**
     * Adds a vehicle to this lane's queue.
     */
    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    /**
     * Removes a vehicle from this lane's queue.
     */
    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
    }

    /**
     * Returns the number of vehicles currently in this lane.
     */
    public int getVehicleCount() {
        return vehicles.size();
    }

    /**
     * Returns the number of waiting vehicles in this lane.
     */
    public int getWaitingCount() {
        int count = 0;
        for (Vehicle v : vehicles) {
            if (v.getStatus() == VehicleStatus.WAITING) count++;
        }
        return count;
    }

    /**
     * Checks if any emergency vehicle is waiting in this lane.
     */
    public boolean hasEmergencyVehicle() {
        for (Vehicle v : vehicles) {
            if (v.isEmergency() && v.getStatus() == VehicleStatus.WAITING) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the maximum wait time among vehicles in this lane.
     */
    public long getMaxWaitTime() {
        long maxWait = 0;
        for (Vehicle v : vehicles) {
            long wt = v.getWaitTime();
            if (wt > maxWait) maxWait = wt;
        }
        return maxWait;
    }

    /**
     * Returns a snapshot list of all vehicles in this lane.
     */
    public List<Vehicle> getVehicles() {
        return new ArrayList<>(vehicles);
    }

    public Direction getDirection() { return direction; }
    public TrafficLight getLight() { return light; }
    public void setLight(TrafficLight light) { this.light = light; }

    /**
     * Clears all vehicles from this lane.
     */
    public void clear() {
        vehicles.clear();
        light = TrafficLight.RED;
    }
}
