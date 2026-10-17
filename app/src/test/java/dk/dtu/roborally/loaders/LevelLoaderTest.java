package dk.dtu.roborally.loaders;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.StartingPoint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LevelLoaderTest {

	@Test
	void loadsStartingPointWithPositionAndOrientation() {
		Board board = LevelLoader.load("levels/level_0.json");

		Space space = board.getSpace(2, 18);
		assertNotNull(space, "The board should contain space (5, 0)");

		var startingPoints = space.getBoardElements().stream()
				.filter(StartingPoint.class::isInstance)
				.map(StartingPoint.class::cast).toList();

		assertEquals(1, startingPoints.size(),
				"Space (5, 0) should contain exactly one starting point");

		StartingPoint startingPoint = startingPoints.get(0);

		assertEquals(new Vector2D(2, 18), startingPoint.getPosition());
		assertEquals(Direction.NORTH, startingPoint.getOrientation());
	}

	@Test
	void boardReturnsStartingPointsInStableOrder() {
		Board board = LevelLoader.load("levels/level_0.json");

		List<Vector2D> positions = board.getStartingPoints().stream()
				.map(StartingPoint::getPosition).toList();

		assertEquals(List.of(new Vector2D(2, 18), new Vector2D(5, 18),
				new Vector2D(8, 18), new Vector2D(11, 18), new Vector2D(14, 18),
				new Vector2D(17, 18)), positions);
	}
}
