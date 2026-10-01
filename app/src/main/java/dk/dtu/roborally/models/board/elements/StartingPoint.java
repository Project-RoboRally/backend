package dk.dtu.roborally.models.board.elements;

import dk.dtu.roborally.models.board.Vector2D;
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
