package dk.dtu.roborally.models;

import lombok.Getter;

/**
 * Represents a checkpoint on the game board.
 * Checkpoints must be reached by players in the required order.
 *
 * @author Matthias
 */
public class Checkpoint extends BoardElement {

    @Getter
    private final int number;

    public Checkpoint(int number) {
        this.number = number;
    }
}