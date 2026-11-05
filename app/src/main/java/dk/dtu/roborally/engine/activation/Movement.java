package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.Pit;
import dk.dtu.roborally.models.board.elements.PriorityAntenna;
import dk.dtu.roborally.models.board.elements.Wall;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Moves robots around the board one space at a time. Moving robots push the
 * robots in front of them, and robots falling into pits or off the board
 * reboot.
 *
 * @author Sebastian
 */
public final class Movement {

	private Movement() {
	}

	/**
	 * Moves the robot one space in the given direction. Robots in the way get
	 * pushed along, however many are lined up. If anything in the line would
	 * hit a wall or the antenna, nobody moves and this returns false. Robots
	 * that end up in a pit or off the board reboot, and if that happens to the
	 * moving robot its move is over, so this returns false too.
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
			Optional<Player> inTheWay = board.isWithinBoard(next)
					? robotAt(game, next)
					: Optional.empty();
			if (inTheWay.isEmpty()) {
				break;
			}
			line.add(inTheWay.get());
			position = next;
		}
		boolean moverFell = false;
		for (Player moving : line) {
			Robot robot = moving.getRobot();
			Vector2D from = robot.getCurrentSpace();
			Vector2D to = from.add(direction.step());
			robot.moveTo(to);
			Optional<Vector2D> fellFrom = fall(board, from, to);
			if (fellFrom.isPresent()) {
				Reboot.reboot(game, moving, fellFrom.get());
				moverFell |= moving == player;
			}
		}
		return !moverFell;
	}

	/**
	 * Where a robot that just moved fell from, if it fell. Off the board it
	 * counts from the last space it was on, a pit is on the board itself.
	 */
	private static Optional<Vector2D> fall(Board board, Vector2D from,
			Vector2D to) {
		if (!board.isWithinBoard(to)) {
			return Optional.of(from);
		}
		if (hasPit(board.getSpace(to))) {
			return Optional.of(to);
		}
		return Optional.empty();
	}

	/**
	 * Whether a robot can not go from one space to the next. Walls and the
	 * priority antenna stop it, nothing can stand on the antenna. The board
	 * edge does not, the robot just falls off.
	 */
	private static boolean blocked(Board board, Vector2D from, Vector2D to,
			Direction direction) {
		return wallBlocks(board, from, to, direction)
				|| hasAntenna(board.getSpace(to));
	}

	private static boolean hasPit(Space space) {
		return space != null && space.getBoardElements().stream()
				.anyMatch(Pit.class::isInstance);
	}

	public static boolean hasAntenna(Space space) {
		return space != null && space.getBoardElements().stream()
				.anyMatch(PriorityAntenna.class::isInstance);
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
