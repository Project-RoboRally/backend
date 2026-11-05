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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Moves robots around the board one space at a time. Moving robots push the
 * robots in front of them.
 *
 * @author Sebastian
 */
public final class Movement {

	private Movement() {
	}

	/**
	 * Moves the robot one space in the given direction. Robots in the way get
	 * pushed along, however many are lined up. If anything in the line would
	 * hit a wall or the edge of the board, nobody moves and this returns false.
	 */
	public static boolean step(Game game, Player player, Direction direction) {
		Board board = game.getBoard();
		List<Player> line = new ArrayList<>();
		line.add(player);
		Vector2D position = player.getRobot().getCurrentSpace();
		while (true) {
			Vector2D next = position.add(direction.step());
			if (blocked(board, position, next, direction)) {
				return false;
			}
			Optional<Player> inTheWay = robotAt(game, next);
			if (inTheWay.isEmpty()) {
				break;
			}
			line.add(inTheWay.get());
			position = next;
		}
		for (Player moving : line) {
			Robot robot = moving.getRobot();
			robot.moveTo(robot.getCurrentSpace().add(direction.step()));
		}
		return true;
	}

	/** Whether a robot can not go from one space to the next. */
	private static boolean blocked(Board board, Vector2D from, Vector2D to,
			Direction direction) {
		return !board.isWithinBoard(to)
				|| wallBlocks(board, from, to, direction);
	}

	public static Optional<Player> robotAt(Game game, Vector2D position) {
		return game.getPlayersInGame().stream().filter(
				player -> position.equals(player.getRobot().getCurrentSpace()))
				.findFirst();
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
}
