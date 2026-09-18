package dk.dtu.roborally.models;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * Represents the Robo Rally game board and its spaces.
 *
 * @author Elias, Matthias, Victor
 */
public class Board {
    @Getter
    Map<Vector2D, Space> spaceMap = new HashMap<>();

    public Board(Map<Vector2D, Space> tileMap) {
        this.spaceMap = tileMap;
    }

    public Space getSpace(int x, int y) {
        return spaceMap.get(new Vector2D(x, y));
    }

    public boolean isWithinBoard(Vector2D position) {
        // check if the position is a valid space on the board
        return spaceMap.containsKey(position);
    }
}
