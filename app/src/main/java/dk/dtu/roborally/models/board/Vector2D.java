package dk.dtu.roborally.models.board;

/**
 * Represents an immutable two-dimensional integer coordinate.
 *
 * @author Matthias, Victor, Sebastian
 */

public record Vector2D(int x, int y) {

	public Vector2D add(Vector2D other) {
		return new Vector2D(x + other.x, y + other.y);
	}

	public Vector2D add(int dx, int dy) {
		return new Vector2D(x + dx, y + dy);
	}

	@Override
	public int hashCode() {
		// Dot Product Hashing
		int a1 = 17; // prime
		int a2 = 31; // prime
		int m = 997; // prime
		return (a1 * x + a2 * y) % m;
	}

	/** Spaces between the two positions, counting only straight moves. */
	public int distanceTo(Vector2D other) {
		return Math.abs(x - other.x) + Math.abs(y - other.y);
	}
}
