package dk.dtu.roborally.models;

import dk.dtu.roborally.enums.Direction;
import lombok.Getter;

/**
 * Represents a player's robot and its current state on the board.
 *
 * @author Elias, Matthias, August
 */
public class Robot {

	@Getter
	private Vector2D currentSpace;

	@Getter
	private Direction orientation;

	public Robot(Vector2D space, Direction orientation) {
		this.currentSpace = space;
		this.orientation = orientation;
	}

	// maybe movement should be done from a higher level
	// public void moveForward(){
	// currentSpace.getPosition().add(direction.step());
	// }

	public void turnRight() {
		orientation = orientation.turnRight();
	}

	public void turnLeft() {
		orientation = orientation.turnLeft();
	}

	public void uTurn() {
		orientation = orientation.uTurn();
	}
}
