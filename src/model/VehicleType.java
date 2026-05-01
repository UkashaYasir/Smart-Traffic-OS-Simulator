package model;

import java.awt.Color;

/**
 * Defines the types of vehicles in the simulation.
 * Each type has a display color and emergency status.
 */
public enum VehicleType {
    NORMAL("Car", new Color(52, 152, 219), false),       // Blue
    AMBULANCE("Ambulance", new Color(231, 76, 60), true), // Red
    POLICE("Police", new Color(243, 156, 18), true),      // Orange
    BUS("Bus", new Color(46, 204, 113), false);            // Green

    private final String displayName;
    private final Color color;
    private final boolean emergency;

    VehicleType(String displayName, Color color, boolean emergency) {
        this.displayName = displayName;
        this.color = color;
        this.emergency = emergency;
    }

    public String getDisplayName() { return displayName; }
    public Color getColor() { return color; }
    public boolean isEmergency() { return emergency; }
}
