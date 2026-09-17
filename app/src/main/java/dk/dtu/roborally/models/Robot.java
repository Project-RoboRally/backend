package dk.dtu.roborally.models;

import dk.dtu.roborally.enums.Direction;
import lombok.Getter;

/**
 * Represents a player's robot and its current state on the board.
 *
 * @author August, Elias, Matthias
 */
public class Robot {

	@Getter
	private Space currentSpace;

	@Getter
	private Direction direction;

	public Robot(Space space, Direction direction) {
		this.currentSpace = space;
		this.direction = direction;
	}

	 
	// maybe movement should done from a higher level
	//public void moveForward(){
	//	currentSpace.getPosition().add(direction.step());
	//}

	public void turnRight(){
		direction = direction.turnRight();
	}

	public void turnLeft(){
		direction = direction.turnLeft();
	}

	public void uTurn(){
		direction = direction.uTurn();
	}
}