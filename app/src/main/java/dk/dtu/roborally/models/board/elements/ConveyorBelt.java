package dk.dtu.roborally.models.board.elements;

import dk.dtu.roborally.enums.ConveyorBeltType;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Vector2D;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a conveyor belt placed on a board space. A conveyor belt moves
 * robots in a specified direction.
 *
 * @author Matthias, Victor, August, Sebastian
 */
public class ConveyorBelt extends BoardElement {

	@Getter
	private final Direction direction;

	@Getter
	private final ConveyorBeltType type;

	@Getter
	private final int length;

	@Getter
	private final Vector2D position;

	@Getter
	@Setter
	private Direction convergingDirection;

	public ConveyorBelt(Vector2D position, int length, Direction direction,
			Direction convergingDirection, ConveyorBeltType type) {
		this.direction = direction;
		this.type = type;
		this.position = position;
		this.length = length;
		this.convergingDirection = convergingDirection; // is set on the
														// incoming belt and
														// indicates the
														// direction of the belt
														// it joins.
	}

	public ConveyorBelt(Vector2D position, int length, Direction direction,
			ConveyorBeltType type) {
		this(position, length, direction, Direction.NONE, type);
	}
}
