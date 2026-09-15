package dk.dtu.roborally.models;

/**
 * Represents an immutable two-dimensional integer coordinate.
 * Now with a hashcode based on the x and y position.
 *
 * @author Matthias, Victor
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
        return 67 * x + y;
    }
}