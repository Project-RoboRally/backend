package dk.dtu.roborally.models;

import dk.dtu.roborally.enums.Direction;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a player's robot and its current state on the board.
 *
 * @author Elias, Matthias
 */
public class Robot {

	@Getter
	private Vector2D position;

    @Getter
    @Setter
    private Direction direction;

    @Getter
    @Setter
    private int lives;

    @Getter
    @Setter
    private int damage;

    @Getter
    @Setter
    private boolean active = true;

	public Robot(Vector2D position, Direction direction, int lives) {
		this.position = position;
		this.direction = direction;
		this.lives = lives;
	}

	public void setPosition(Vector2D position) {
		this.position = position;
	}
}
