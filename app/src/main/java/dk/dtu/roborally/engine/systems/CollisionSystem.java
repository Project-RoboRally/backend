package dk.dtu.roborally.engine.systems;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.BoardElement;
import dk.dtu.roborally.models.Game;
import dk.dtu.roborally.models.Player;
import dk.dtu.roborally.models.Robot;
import dk.dtu.roborally.models.Tile;
import dk.dtu.roborally.models.Vector2D;
import dk.dtu.roborally.models.Wall;

/**
 * Detects and resolves collisions involving robots and board elements.
 *
 * @author Sebastian, Victor
 */
public class CollisionSystem {

    // Checks if a robot can move to a target position on the board, considering walls and other robots.
    public boolean canMove(Game game, Robot robot, Vector2D targetPosition) {
        if (robot == null || game == null) {
            return false;
        }

        if (!game.getBoard().isWithinBoard(targetPosition)) {
            return false;
        }

        if (isBlockedByWall(game, robot, targetPosition)) {
            return false;
        }

        for (Player player : game.getPlayersInGame()) {
            Robot other = player.getRobot();
            if (other != null && other != robot && other.getPosition().equals(targetPosition)) {
                return false;
            }
        }

        return true;
    }

    // Checks if the robots movement is blocked by a wall on the board. 
    // The targetposition is the position the robot wants to move to, and the 
    // robots current position is used to determine the direction of movement.
    private boolean isBlockedByWall(Game game, Robot robot, Vector2D targetPosition) {
        Vector2D from = robot.getPosition();
        Direction dir = robot.getDirection();

        Tile currentTile = game.getBoard().getTile(from.x(), from.y());
        Tile targetTile = game.getBoard().getTile(targetPosition.x(), targetPosition.y());

        if (currentTile != null && hasWallOnSide(currentTile, dir)) {
            return true;
        }

        if (targetTile != null && hasWallOnSide(targetTile, opposite(dir))) {
            return true;
        }

        return false;
    }

    // Helper method for wall detection
    private boolean hasWallOnSide(Tile tile, Direction side) {
        if (tile == null) return false;

        for (BoardElement element : tile.getBoardElements()) {
            if (element instanceof Wall wall && wall.getSide() == side) {
                return true;
            }
        }

        return false;
    }

    // This just converts a direction to the opposite direction
    private Direction opposite(Direction dir) {
        return switch (dir) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
        };
    }
}