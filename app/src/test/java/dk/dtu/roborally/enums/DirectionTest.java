package dk.dtu.roborally.enums;

import dk.dtu.roborally.models.board.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest {

	@Test
	void turnsRightClockwise() {
		assertEquals(Direction.EAST, Direction.NORTH.turnRight());
		assertEquals(Direction.SOUTH, Direction.EAST.turnRight());
		assertEquals(Direction.WEST, Direction.SOUTH.turnRight());
		assertEquals(Direction.NORTH, Direction.WEST.turnRight());
	}

	@Test
	void turnsLeftCounterclockwise() {
		assertEquals(Direction.WEST, Direction.NORTH.turnLeft());
		assertEquals(Direction.SOUTH, Direction.WEST.turnLeft());
		assertEquals(Direction.EAST, Direction.SOUTH.turnLeft());
		assertEquals(Direction.NORTH, Direction.EAST.turnLeft());
	}

	@Test
	void uTurnFacesTheOtherWay() {
		assertEquals(Direction.SOUTH, Direction.NORTH.uTurn());
		assertEquals(Direction.WEST, Direction.EAST.uTurn());
		assertEquals(Direction.NORTH, Direction.SOUTH.uTurn());
		assertEquals(Direction.EAST, Direction.WEST.uTurn());
	}

	@Test
	void northIsUpTheBoard() {
		assertEquals(new Vector2D(0, -1), Direction.NORTH.step());
		assertEquals(new Vector2D(1, 0), Direction.EAST.step());
	}
}
