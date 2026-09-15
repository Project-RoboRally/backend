package dk.dtu.roborally.objects;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.ConveyorBeltType;
import lombok.Getter;

/**
 * Represents a conveyor belt placed on a board tile.
 * A conveyor belt moves robots in a specified direction.
 *
 * @author Matthias, Victor
 */
public class ConveyorBelt extends BoardElement {

    @Getter
    private final Direction direction;

    @Getter
    private final ConveyorBeltType type;

    @Getter
    private final int length;

    @Getter
    private final Vector2D position;

    public ConveyorBelt(Vector2D position, int length, Direction direction, ConveyorBeltType type) {
        this.direction = direction;
        this.type = type;
        this.position = position;
        this.length = length;
    }
}