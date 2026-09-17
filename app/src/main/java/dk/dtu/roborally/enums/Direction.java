package dk.dtu.roborally.enums;

import dk.dtu.roborally.models.Vector2D;

/**
 * Represents a direction for a robot.
 *
 * @author August, Matthias
 */
public enum Direction {
	NORTH(new Vector2D(0, -1)),
    EAST(new Vector2D(1, 0)),
    SOUTH(new Vector2D(0, 1)),
    WEST(new Vector2D(-1, 0));

	private final Vector2D vector;

	Direction(Vector2D vector){
		this.vector = vector;
	}

	public Vector2D step(){
		return vector;
	}

	public Direction turnRight(){
		return switch (this){
			case NORTH -> EAST;
			case EAST -> SOUTH;
			case SOUTH -> WEST;
			case WEST -> NORTH;
		};
	}

	public Direction turnLeft(){
		return switch (this){
			case NORTH -> WEST;
			case WEST -> SOUTH;
			case SOUTH -> EAST;
			case EAST -> NORTH;
		};
	}

	public Direction uTurn(){
		return switch (this){
			case NORTH -> SOUTH;
			case SOUTH -> NORTH;
			case EAST -> WEST;
			case WEST -> EAST;
		};
	}

}

