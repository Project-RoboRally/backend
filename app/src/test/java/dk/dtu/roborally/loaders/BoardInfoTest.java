package dk.dtu.roborally.loaders;

import dk.dtu.roborally.helpers.TestResources;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author August
 */
class BoardInfoTest {

	private static final String SMALL_BOARD = "{\"squares\":[{\"x1\":0,\"y1\":0,\"x2\":3,\"y2\":3}]}";

	@Test
	void anUploadedBoardIsNamedFromItsMetadata() {
		BoardInfo board = BoardInfo
				.uploaded(TestResources.read("levels/test_board.json"));

		assertEquals("Test board", board.name());
		assertNull(board.path());
		assertTrue(board.id().startsWith("custom-"));
	}

	@Test
	void anUploadedBoardWithoutANameIsCalledCustomBoard() {
		assertEquals("Custom board", BoardInfo.uploaded(SMALL_BOARD).name());
	}

	@Test
	void theSameFileGivesTheSameIdSoARestoredLobbyKeepsIt() {
		assertEquals(BoardInfo.uploaded(SMALL_BOARD).id(),
				BoardInfo.uploaded(SMALL_BOARD).id());
	}

	@Test
	void aDifferentFileGivesADifferentIdSoThePreviewReloads() {
		String otherBoard = SMALL_BOARD.replace("\"x2\":3", "\"x2\":4");

		assertNotEquals(BoardInfo.uploaded(SMALL_BOARD).id(),
				BoardInfo.uploaded(otherBoard).id());
	}

	@Test
	void anUploadedBoardIsBuiltFromItsJson() {
		assertEquals(9, countSpaces(BoardInfo.uploaded(SMALL_BOARD)));
	}

	@Test
	void textThatIsNotJsonIsRefused() {
		assertThrows(IllegalArgumentException.class,
				() -> BoardInfo.uploaded("not a board"));
	}

	private static int countSpaces(BoardInfo board) {
		var level = board.load();
		int spaces = 0;
		for (int x = 0; x < LevelLoader.MAX_SIDE; x++) {
			for (int y = 0; y < LevelLoader.MAX_SIDE; y++) {
				if (level.getSpace(x, y) != null) {
					spaces++;
				}
			}
		}
		return spaces;
	}
}
