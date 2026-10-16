package dk.dtu.roborally.models.board.elements;

import dk.dtu.roborally.models.board.Vector2D;
import lombok.Getter;

/**
 * Represents the priority antenna of a game, which is a board element that
 * occupies a space, meaning no other elements or robots can be on the same
 * space. The priority antenna calculates, which players registers should be
 * activated first at each register. This priority order is calculated by
 * scanning clockwise starting at 12 o'clock and making a queue based on the
 * robots distance to the priority antenna.
 *
 * @author August, Victor
 */
public class PriorityAntenna extends BoardElement {

	@Getter
	private final Vector2D position;

	public PriorityAntenna(Vector2D position) {
		this.position = position;
	}
}
