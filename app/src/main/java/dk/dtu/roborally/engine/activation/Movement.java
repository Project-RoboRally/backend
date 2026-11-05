package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.Wall;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;

/**
 * Moves robots around the board one space at a time.
 *
 * @author Sebastian
 */
public final class Movement {

	private Movement() {
	}

	/**
	 * One space along the given direction. Off the board, a wall on the crossed
	 * edge, or another robot blocks the step.
	 */
	public static boolean step(Game game, Player player, Direction direction) {
		Robot robot = player.getRobot();
		Vector2D from = robot.getCurrentSpace();
		Vector2D to = from.add(direction.step());
		Board board = game.getBoard();

		if (!board.isWithinBoard(to) || wallBlocks(board, from, to, direction)
				|| occupied(game, to, player)) {
			return false;
		}

		robot.moveTo(to);
		return true;
	}

	private static boolean wallBlocks(Board board, Vector2D from, Vector2D to,
			Direction direction) {
		if (hasWall(board.getSpace(from), direction)) {
			return true;
		}
		return hasWall(board.getSpace(to), direction.uTurn());
	}

	private static boolean hasWall(Space space, Direction side) {
		if (space == null) {
			return false;
		}
		for (BoardElement element : space.getBoardElements()) {
			if (element instanceof Wall wall && wall.getSide() == side) {
				return true;
			}
		}
		return false;
	}

	private static boolean occupied(Game game, Vector2D position,
			Player moving) {
		for (Player other : game.getPlayersInGame()) {
			if (other != moving
					&& position.equals(other.getRobot().getCurrentSpace())) {
				return true;
			}
		}
		return false;
	}
}
