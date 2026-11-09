package dk.dtu.roborally.loaders;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.PriorityAntenna;
import dk.dtu.roborally.models.board.elements.PushPanel;
import dk.dtu.roborally.models.board.elements.RebootPoint;
import dk.dtu.roborally.models.board.elements.StartingPoint;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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

	@Test
	void loadsPriorityAntenna() {
		Board board = LevelLoader.load("levels/level_0.json");

		PriorityAntenna antenna = board.getSpace(10, 17).getBoardElements()
				.stream().filter(PriorityAntenna.class::isInstance)
				.map(PriorityAntenna.class::cast).findFirst().orElseThrow();
		assertEquals(Direction.NORTH, antenna.getFacing());
	}

	@Test
	void loadAllowsMissingOptionalLevelObjects() {
		assertNotNull(assertDoesNotThrow(() -> LevelLoader
				.load("levels/level_without_optional_objects.json")));
	}

	@Test
	void loadRejectsMissingSquares() {
		IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> LevelLoader.load("levels/level_without_squares.json"));
		assertTrue(thrown.getMessage()
				.contains("Level must contain at least one square"));
	}

	@Test
	void rejectsUnknownDirection() {
		IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> LevelLoader.load("levels/level_with_bad_direction.json"));
		assertEquals("Invalid wall direction: up", thrown.getMessage());
	}

	@Test
	void loadsTheRebootTokenAndTheAreaItCovers() {
		Board board = LevelLoader.load("levels/level_0.json");

		RebootPoint reboot = board.getElements(RebootPoint.class).get(0);

		assertEquals(new Vector2D(17, 15), reboot.getPosition());
		assertEquals(Direction.NORTH, reboot.getDirection());
		assertTrue(reboot.covers(new Vector2D(0, 16)));
		assertFalse(reboot.covers(new Vector2D(5, 18)));
	}

	@Test
	void pushPanelsKnowTheirRegisters() {
		Board board = LevelLoader.load("levels/level_0.json");

		PushPanel panel = board.getSpace(19, 8).getBoardElements().stream()
				.filter(PushPanel.class::isInstance).map(PushPanel.class::cast)
				.findFirst().orElseThrow();

		assertEquals(Set.of(2, 4), panel.getRegisters());
		assertTrue(panel.pushesIn(1));
		assertFalse(panel.pushesIn(2));
	}
}
