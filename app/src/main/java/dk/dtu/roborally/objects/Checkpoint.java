package dk.dtu.roborally.objects;

import lombok.Getter;

/**
 * Represents a checkpoint on the game board.
 * Checkpoints must be reached by players in the required order.
 *
 * @author Matthias, Victor
 */
public class Checkpoint extends BoardElement {

    @Getter
    private final int number;

    @Getter
    private final Vector2D position;

    public Checkpoint(Vector2D position, int number) {
        this.position = position;
        this.number = number;
    }
}