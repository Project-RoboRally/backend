package dk.dtu.roborally.models;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * Represents the Robo Rally game board and its tiles.
 *
 * @author Elias, Matthias, Victor
 */
public class Board {
    @Getter
    Map<Vector2D, Tile> tileMap = new HashMap<>();

    public Board(Map<Vector2D, Tile> tileMap) {
        this.tileMap = tileMap;
    }

    public Tile getTile(int x, int y) {
        return tileMap.get(new Vector2D(x, y));
    }

    public boolean isWithinBoard(Vector2D position) {
        // check if the position is a valid space on the board
        return tileMap.containsKey(position);
    }
}
