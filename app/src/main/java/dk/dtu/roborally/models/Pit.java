package dk.dtu.roborally.models;

import dk.dtu.roborally.enums.Direction;
import lombok.Getter;

/**
 * Represents a pit on the game board.
 * A robot entering a pit is destroyed or loses a life according to the game
 * rules.
 *
 * @author Matthias, Victor
 */
public class Pit extends BoardElement {
    @Getter
    private final Vector2D position;

    public Pit(Vector2D position) {
        this.position = position;
    }
}