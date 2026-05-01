package model;

import java.awt.Color;

/**
 * Represents traffic light states: RED, YELLOW, GREEN.
 * Each state has an associated display color.
 */
public enum TrafficLight {
    RED(new Color(231, 76, 60)),
    YELLOW(new Color(241, 196, 15)),
    GREEN(new Color(46, 204, 113));

    private final Color color;

    TrafficLight(Color color) {
        this.color = color;
    }

    public Color getColor() { return color; }
}
