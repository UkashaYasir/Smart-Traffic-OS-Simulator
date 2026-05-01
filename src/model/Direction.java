package model;

/**
 * Represents the four directions of the traffic intersection.
 * Each direction corresponds to a lane where vehicles approach from.
 */
public enum Direction {
    NORTH("North"),
    SOUTH("South"),
    EAST("East"),
    WEST("West");

    private final String displayName;

    Direction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the opposite direction.
     * Used for paired traffic light control (N-S and E-W).
     */
    public Direction opposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
            default: return this;
        }
    }

    /**
     * Checks if this direction is in the North-South axis.
     */
    public boolean isNorthSouth() {
        return this == NORTH || this == SOUTH;
    }
}
