package dk.dtu.roborally.models;

import lombok.Getter;

/**
 * Represents the Robo Rally game board and its spaces.
 *
 * @author Elias, Matthias
 */
public class Board {

	@Getter
	private final Vector2D dimensions;

	@Getter
	private final Space[][] spaces;

	public Board(int width, int height) {
		this.dimensions = new Vector2D(width, height);
		this.spaces = new Space[height][width];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				spaces[y][x] = new Space(new Vector2D(x, y));
			}
		}
	}

	public Space getSpace(Vector2D position) {
		return spaces[position.y()][position.x()];
	}

	public boolean contains(Vector2D position) {
		return position.x() >= 0 && position.x() < dimensions.x()
				&& position.y() >= 0 && position.y() < dimensions.y();
	}
}
