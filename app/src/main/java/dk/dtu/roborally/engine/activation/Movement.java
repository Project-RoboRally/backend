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
import java.util.function.Consumer;

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
		return step(game, player, direction, false);
	}

	/**
	 * Same as {@link #step(Game, Player, Direction)}, but a hovering robot
	 * passes over pits instead of falling in. Robots it pushes still fall.
	 */
	public static boolean step(Game game, Player player, Direction direction,
			boolean hovering) {
		return step(game, player, direction, hovering, pushed -> {
		});
	}

	/**
	 * Same as {@link #step(Game, Player, Direction, boolean)}, and tells onPush
	 * about the robot this one pushed directly, if any.
	 */
	public static boolean step(Game game, Player player, Direction direction,
			boolean hovering, Consumer<Player> onPush) {
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
		if (line.size() > 1) {
			onPush.accept(line.get(1));
		}
		boolean moverFell = false;
		for (Player moving : line) {
			Robot robot = moving.getRobot();
			Vector2D from = robot.getCurrentSpace();
			robot.moveTo(from.add(direction.step()));
			if (hovering && moving == player) {
				moverFell |= fallOffTheBoard(game, moving, from);
			} else {
				moverFell |= fallIfNeeded(game, moving, from)
						&& moving == player;
			}
		}
		return !moverFell;
	}

	/**
	 * Reboots a robot that just moved from the given space into a pit or off
	 * the board. Off the board it counts as falling from the last space it was
	 * on, a pit is on the board itself. Returns whether it fell.
	 */
	private static boolean fallOffTheBoard(Game game, Player player,
			Vector2D from) {
		if (game.getBoard()
				.isWithinBoard(player.getRobot().getCurrentSpace())) {
			return false;
		}
		Reboot.reboot(game, player, true, from);
		return true;
	}

	/** A hovering robot that ended its move over a pit falls in now. */
	public static void landOnPit(Game game, Player player) {
		Vector2D position = player.getRobot().getCurrentSpace();
		if (!player.getRobot().isRebootedThisRound()
				&& hasPit(game.getBoard().getSpace(position))) {
			Reboot.reboot(game, player, true, position);
		}
	}

	static boolean fallIfNeeded(Game game, Player player, Vector2D from) {
		Board board = game.getBoard();
		Vector2D to = player.getRobot().getCurrentSpace();
		if (!board.isWithinBoard(to)) {
			Reboot.reboot(game, player, true, from);
			return true;
		}
		if (hasPit(board.getSpace(to))) {
			Reboot.reboot(game, player, true, to);
			return true;
		}
		return false;
	}

	/**
	 * Whether a robot can not go from one space to the next. Walls and the
	 * priority antenna stop it, nothing can stand on the antenna. The board
	 * edge does not, the robot just falls off.
	 */
	static boolean blocked(Board board, Vector2D from, Vector2D to,
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

	static boolean wallBlocks(Board board, Vector2D from, Vector2D to,
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
