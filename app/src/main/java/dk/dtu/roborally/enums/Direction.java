package dk.dtu.roborally.enums;

import dk.dtu.roborally.models.board.Vector2D;

/**
 * Represents a direction for a robot. Declared in clockwise order, which the
 * turns rely on.
 *
 * @author August, Matthias, Sebastian
 */
public enum Direction {
	NORTH(0, -1), EAST(1, 0), SOUTH(0, 1), WEST(-1, 0);

	private final Vector2D step;

	Direction(int dx, int dy) {
		this.step = new Vector2D(dx, dy);
	}

	public Vector2D step() {
		return step;
	}

	public Direction turnRight() {
		return rotate(1);
	}

	public Direction turnLeft() {
		return rotate(3);
	}

	public Direction uTurn() {
		return rotate(2);
	}

	private Direction rotate(int quarterTurns) {
		Direction[] directions = values();
		return directions[(ordinal() + quarterTurns) % directions.length];
	}
}
