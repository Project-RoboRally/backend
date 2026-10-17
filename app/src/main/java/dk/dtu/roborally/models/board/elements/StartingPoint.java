package dk.dtu.roborally.models.board.elements;

import java.util.Objects;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Vector2D;
import lombok.Getter;

/**
 * Represents one of 6 starting points on the game board.
 *
 * @author Victor, August
 */
@Getter
public class StartingPoint extends BoardElement {

	private final Vector2D position;
	private final Direction orientation;

	public StartingPoint(Vector2D position, Direction orientation) {
		this.position = Objects.requireNonNull(position);
		this.orientation = Objects.requireNonNull(orientation);
	}
}
