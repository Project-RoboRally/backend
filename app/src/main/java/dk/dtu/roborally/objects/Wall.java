package dk.dtu.roborally.objects;

import dk.dtu.roborally.enums.Direction;
import lombok.Getter;

/**
 * Represents a wall positioned along one side of a board tile.
 * Walls block robot movement and laser beams.
 *
 * @author Matthias
 */
public class Wall extends BoardElement {

    @Getter
    private final Direction side;

    public Wall(Direction side) {
        this.side = side;
    }
}