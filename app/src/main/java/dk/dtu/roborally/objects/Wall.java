package dk.dtu.roborally.objects;

import dk.dtu.roborally.enums.Direction;
import lombok.Getter;

/**
 * Represents a wall positioned along one side of a board tile.
 * Walls block robot movement and laser beams.
 *
 * @author Matthias, Victor
 */
public class Wall extends BoardElement {

    @Getter
    private final Direction side;

    @Getter
    private final Vector2D position;

    public Wall(Vector2D position, Direction side) {
        this.position = position;
        this.side = side;
    }
}