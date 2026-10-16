package dk.dtu.roborally.engine.systems;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.Wall;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;

/**
 * Detects and resolves collisions involving robots and board elements.
 *
 * @author Sebastian, Victor
 */
public class CollisionSystem {

	// Checks if a robot can move to a target position on the board, considering
	// walls and other robots.
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
			if (other != null && other != robot
					&& other.getCurrentSpace().equals(targetPosition)) {
				return false;
			}
		}

		return true;
	}

	// Checks if the robots movement is blocked by a wall on the board.
	// The targetposition is the position the robot wants to move to, and the
	// robots current position is used to determine the direction of movement.
	private boolean isBlockedByWall(Game game, Robot robot,
			Vector2D targetPosition) {
		Vector2D from = robot.getCurrentSpace();
		Direction dir = robot.getOrientation();

		Space currentSpace = game.getBoard().getSpace(from);
		Space targetSpace = game.getBoard().getSpace(targetPosition);

		if (currentSpace != null && hasWallOnSide(currentSpace, dir)) {
			return true;
		}

		if (targetSpace != null && hasWallOnSide(targetSpace, opposite(dir))) {
			return true;
		}

		return false;
	}

	// Helper method for wall detection
	private boolean hasWallOnSide(Space space, Direction side) {
		if (space == null)
			return false;

		for (BoardElement element : space.getBoardElements()) {
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
