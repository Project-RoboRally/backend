package dk.dtu.roborally.objects;

import dk.dtu.roborally.enums.Direction;
import lombok.Getter;

/**
 * Represents a push panel positioned along one side of a board tile.
 * push panels block robot movement and laser beams as well as push a player who
 * lands on them.
 *
 * @author Victor
 */
public class PushPanel extends BoardElement {

    @Getter
    private final Direction pushDirection;

    @Getter
    private final Vector2D position;

    public PushPanel(Vector2D position, Direction pushDirection) {
        this.position = position;
        this.pushDirection = pushDirection;
    }
}