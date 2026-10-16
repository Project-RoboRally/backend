package dk.dtu.roborally.models.player;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Vector2D;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a player's robot and its current state on the board.
 *
 * @author Elias, Matthias, August, Sebastian
 */
public class Robot {

	@Getter
	@Setter
	private Vector2D currentSpace;

	@Getter
	private Direction orientation;

	@Getter
	private boolean rebootedThisRound;

	@Getter
	@Setter
	private int lives;

	@Getter
	@Setter
	private int damage;

	@Getter
	@Setter
	private boolean active = true;

	public Robot(Vector2D space, Direction orientation) {
		this.currentSpace = space;
		this.orientation = orientation;
	}

	public Robot(Vector2D space, Direction orientation, int lives) {
		this.currentSpace = space;
		this.orientation = orientation;
		this.lives = lives;
	}

	public void turnRight() {
		orientation = orientation.turnRight();
	}

	public void turnLeft() {
		orientation = orientation.turnLeft();
	}

	public void uTurn() {
		orientation = orientation.uTurn();
	}

	public void turnAround() {
		uTurn();
	}

	public void moveForward(int spaces) {
		Vector2D step = orientation.step();
		currentSpace = currentSpace.add(step.x() * spaces, step.y() * spaces);
	}
}
