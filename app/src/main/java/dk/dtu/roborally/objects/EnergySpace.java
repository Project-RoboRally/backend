package dk.dtu.roborally.objects;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a energy space on the game board.
 * A robot standing on a energy space claims the cubes and gets an additional
 * energy cube by standing on there even if there are no cubes left.
 *
 * @author Victor
 */
public class EnergySpace extends BoardElement {
    @Getter
    private final Vector2D position;

    @Getter
    @Setter
    private int cubeAmount;

    public EnergySpace(Vector2D position, int cubeAmount) {
        this.position = position;
        this.cubeAmount = cubeAmount;
    }
}