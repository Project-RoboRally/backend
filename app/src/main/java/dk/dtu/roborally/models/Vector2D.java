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

    // Since we want a HashMap with the tiles (a tile map)
    // We need some kind of hashing function, since Vector2D has two variables
    @Override
    public int hashCode() {
        return (x * 123456789) + y;
    }
}