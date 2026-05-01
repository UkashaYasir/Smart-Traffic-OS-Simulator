package model;

/**
 * Represents the current status of a vehicle in the simulation.
 * Tracks lifecycle from creation to passing through the intersection.
 */
public enum VehicleStatus {
    CREATED("Created"),
    APPROACHING("Approaching"),
    WAITING("Waiting"),
    IN_INTERSECTION("In Intersection"),
    PASSED("Passed"),
    BLOCKED("Blocked/Deadlocked");

    private final String displayName;

    VehicleStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
