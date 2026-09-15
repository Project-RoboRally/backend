package dk.dtu.roborally.models;

import lombok.Getter;

/**
 * Represents the Robo Rally game board and its tiles.
 *
 * @author Elias, Matthias
 */
public class Board {

	@Getter
	private final Vector2D dimensions;

	@Getter
	private final Tile[][] tiles;

	public Board(int width, int height) {
		this.dimensions = new Vector2D(width, height);
		this.tiles = new Tile[height][width];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				tiles[y][x] = new Tile(new Vector2D(x, y));
			}
		}
	}

	public Tile getTile(Vector2D position) {
		return tiles[position.y()][position.x()];
	}

	public boolean contains(Vector2D position) {
		return position.x() >= 0 && position.x() < dimensions.x()
				&& position.y() >= 0 && position.y() < dimensions.y();
	}
}
