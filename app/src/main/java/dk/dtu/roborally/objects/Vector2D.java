package dk.dtu.roborally.objects;

public record Vector2D(int x, int y) {

    public Vector2D set(Vector2D vector2D) {
        return new Vector2D(vector2D.x, vector2D.y);
    }

    public Vector2D set(int x, int y) {
        return new Vector2D(x, y);
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    public Vector2D add(int dx, int dy) {
        return new Vector2D(x + dx, y + dy);
    }
}