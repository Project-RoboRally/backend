package dk.dtu.roborally.models.board;

import dk.dtu.roborally.enums.ConveyorBeltType;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.elements.Checkpoint;
import dk.dtu.roborally.models.board.elements.ConveyorBelt;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoardTest {

	@Test
	void listsAnElementCoveringSeveralSpacesOnce() {
		Map<Vector2D, Space> spaces = new HashMap<>();
		ConveyorBelt belt = new ConveyorBelt(new Vector2D(0, 0), 2,
				Direction.EAST, ConveyorBeltType.GREEN_STRAIGHT);
		Checkpoint checkpoint = new Checkpoint(new Vector2D(0, 1), 1);
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				spaces.put(new Vector2D(x, y), new Space(new Vector2D(x, y)));
			}
		}
		spaces.get(new Vector2D(0, 0)).addBoardElement(belt);
		spaces.get(new Vector2D(1, 0)).addBoardElement(belt);
		spaces.get(new Vector2D(0, 1)).addBoardElement(checkpoint);

		Board board = new Board(spaces);

		assertEquals(List.of(belt), board.getElements(ConveyorBelt.class));
		assertEquals(List.of(checkpoint), board.getElements(Checkpoint.class));
	}
}
