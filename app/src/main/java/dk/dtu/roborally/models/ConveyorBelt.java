package dk.dtu.roborally.models;

import dk.dtu.roborally.enums.ConveyorBeltType;
import dk.dtu.roborally.enums.Direction;
import lombok.Getter;

/**
 * Represents a conveyor belt placed on a board tile. A conveyor belt moves
 * robots in a specified direction.
 *
 * @author Matthias
 */
public class ConveyorBelt extends BoardElement {

	@Getter
	private final Direction direction;

	@Getter
	private final ConveyorBeltType type;

	public ConveyorBelt(Direction direction, ConveyorBeltType type) {
		this.direction = direction;
		this.type = type;
	}
}
