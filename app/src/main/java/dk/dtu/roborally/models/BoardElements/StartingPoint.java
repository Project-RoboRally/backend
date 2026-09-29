package dk.dtu.roborally.models.BoardElements;

import dk.dtu.roborally.models.Vector2D;
import lombok.Getter;

/**
 * Represents one of five starting points on the game board.
 *
 * @author Victor
 */
public class StartingPoint extends BoardElement {

	@Getter
	private final Vector2D position;

	public StartingPoint(Vector2D position) {
		this.position = position;
	}
}
