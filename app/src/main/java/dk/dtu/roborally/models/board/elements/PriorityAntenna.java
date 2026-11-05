package dk.dtu.roborally.models.board.elements;

import dk.dtu.roborally.enums.Direction;
import lombok.Getter;
import java.util.Objects;

/**
 * Represents the priority antenna of a game, which is a board element that
 * occupies a space, meaning no other elements or robots can be on the same
 * space. The priority antenna calculates, which players registers should be
 * activated first at each register. Robots closer to it go first. Ties are
 * broken by scanning clockwise, starting where the dish points.
 *
 * @author August, Sebastian
 */
public class PriorityAntenna extends BoardElement {

	@Getter
	private final Direction facing;

	/** An antenna with its dish pointing north, 12 o'clock. */
	public PriorityAntenna() {
		this(Direction.NORTH);
	}

	public PriorityAntenna(Direction facing) {
		this.facing = Objects.requireNonNull(facing);
	}
}
