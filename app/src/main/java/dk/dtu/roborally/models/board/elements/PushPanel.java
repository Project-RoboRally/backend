package dk.dtu.roborally.models.board.elements;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Vector2D;
import lombok.Getter;
import java.util.Set;

/**
 * Represents a push panel positioned along one side of a board tile. push
 * panels block robot movement and laser beams as well as push a player who
 * lands on them.
 *
 * @author Victor, Sebastian
 */
public class PushPanel extends BoardElement {

	@Getter
	private final Direction pushDirection;

	@Getter
	private final Vector2D position;

	/** The registers the panel pushes in, 1 to 5 like printed on it. */
	@Getter
	private final Set<Integer> registers;

	public PushPanel(Vector2D position, Direction pushDirection,
			Set<Integer> registers) {
		this.position = position;
		this.pushDirection = pushDirection;
		this.registers = Set.copyOf(registers);
	}

	/** Whether it pushes in the register with this index, counting from 0. */
	public boolean pushesIn(int registerIndex) {
		return registers.contains(registerIndex + 1);
	}
}
