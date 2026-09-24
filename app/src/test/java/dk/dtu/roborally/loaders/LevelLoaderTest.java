package dk.dtu.roborally.loaders;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import dk.dtu.roborally.models.Board;

class LevelLoaderTest {

    @Test
    void loadAllowsMissingOptionalLevelObjects() {
        Board board = assertDoesNotThrow(() -> LevelLoader.load("levels/level_without_optional_objects.json"));
        assertNotNull(board);
    }
}
