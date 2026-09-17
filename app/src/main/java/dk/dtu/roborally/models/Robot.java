package dk.dtu.roborally.models;

import dk.dtu.roborally.enums.Direction;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a player's robot and its current state on the board.
 *
 * @author Elias, Matthias
 */
public class Robot {

    @Getter
    private Vector2D position;

    @Getter
    @Setter
    private Direction direction;

    @Getter
    @Setter
    private int lives;

    @Getter
    @Setter
    private int damage;

    @Getter
    @Setter
    private boolean active = true;

    public Robot(Vector2D position, Direction direction, int lives) {
        this.position = position;
        this.direction = direction;
        this.lives = lives;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public void moveForward(int spaces) {
        Vector2D movement = switch (direction) {
            case NORTH -> new Vector2D(0, -spaces);
            case EAST -> new Vector2D(spaces, 0);
            case SOUTH -> new Vector2D(0, spaces);
            case WEST -> new Vector2D(-spaces, 0);
        };
        position = position.add(movement);
    }

    public void turnLeft() {
        direction = switch (direction) {
            case NORTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
        };
    }

    public void turnRight() {
        direction = switch (direction) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
        };
    }

    public void turnAround() {
        turnRight();
        turnRight();
    }
}