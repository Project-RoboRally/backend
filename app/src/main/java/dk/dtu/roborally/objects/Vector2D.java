package dk.dtu.roborally.objects;

/**
 * Represents an immutable two-dimensional integer coordinate.
 *
 * @author Matthias
 */

public record Vector2D(int x, int y) {

	public Vector2D add(Vector2D other) {
		return new Vector2D(x + other.x, y + other.y);
	}

	public Vector2D add(int dx, int dy) {
		return new Vector2D(x + dx, y + dy);
	}
}
