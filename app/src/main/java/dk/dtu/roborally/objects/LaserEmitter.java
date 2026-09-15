package dk.dtu.roborally.objects;

import dk.dtu.roborally.enums.Direction;
import lombok.Getter;

/**
 * Represents a laser emitter placed on the game board.
 * A laser emitter fires in a specified direction and can damage robots.
 *
 * @author Matthias, Victor
 */
public class LaserEmitter extends BoardElement {

    @Getter
    private final Direction direction;

    @Getter
    private final Vector2D position;

    @Getter
    private final int damage;

    public LaserEmitter(Vector2D position, Direction direction) {
        this.position = position;
        this.direction = direction;
        this.damage = 1;
    }
}