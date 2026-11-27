package dk.dtu.roborally.loaders;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardCatalogTest {

	private final BoardCatalog catalog = BoardCatalog.load();

	@Test
	void listsEveryBoardInConfigOrderWithItsName() {
		assertEquals(
				List.of(new BoardInfo("level_0", "Conveyor cyclus",
						"levels/level_0.json"),
						new BoardInfo("test_board", "Test board",
								"levels/test_board.json"),
						new BoardInfo("corridor_blitz", "Corridor Blitz",
								"levels/corridor_blitz.json"),
						new BoardInfo("dizzy_highway", "Dizzy Highway",
								"levels/dizzy_highway.json")),

				catalog.all());
	}

	@Test
	void theFirstBoardIsTheDefault() {
		assertEquals("level_0", catalog.defaultBoard().id());
	}

	@Test
	void unknownIdFindsNothing() {
		assertTrue(catalog.byId("no_such_board").isEmpty());
	}

	@Test
	void aStoredPathFindsItsBoard() {
		assertEquals("test_board",
				catalog.forStoredPath("levels/test_board.json").id());
	}

	@Test
	void aStoredPathThatIsNoLongerListedStillLoads() {
		String path = "levels/level_without_optional_objects.json";

		BoardInfo board = catalog.forStoredPath(path);

		assertEquals(path, board.path());
	}

	@Test
	void duplicateIdsAreRejected() {
		BoardInfo board = new BoardInfo("a", "A", "levels/level_0.json");

		assertThrows(IllegalStateException.class,
				() -> new BoardCatalog(List.of(board, board)));
	}

	@Test
	void anEmptyListIsRejected() {
		assertThrows(IllegalStateException.class,
				() -> new BoardCatalog(List.of()));
	}
}
