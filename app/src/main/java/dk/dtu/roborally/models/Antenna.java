package dk.dtu.roborally.models;

import lombok.Getter;

/**
 * This determines the position of the antenna on the board. 
 * The antenna is used to determine the starting position of the robot.
 *
 * @author Victor
 */
public class Antenna extends BoardElement {

    @Getter
    private final Vector2D position;

    public Antenna(Vector2D position) {
        this.position = position;
    }
}