package dk.dtu.roborally.loaders;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

import dk.dtu.roborally.models.Board;

class LevelLoaderTest {

    @Test
    void loadAllowsMissingOptionalLevelObjects() {
        Board board = assertDoesNotThrow(() -> LevelLoader.load("levels/level_without_optional_objects.json"));
        assertNotNull(board);
    }

    @Test
    void loadRejectsMissingSquares() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> LevelLoader.load("levels/level_without_squares.json"),
                "Expected LevelLoader.load() to throw, but it didn't");
        assumeTrue(thrown.getMessage().contains("Level must contain at least one square: "));
    }
}
