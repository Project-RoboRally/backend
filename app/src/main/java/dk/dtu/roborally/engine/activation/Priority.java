package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.PriorityAntenna;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Works out whose turn it is. The robot closest to the priority antenna goes
 * first. Robots just as close are taken clockwise, starting from where the dish
 * points. Without an antenna it goes by username.
 *
 * @author Sebastian
 */
public final class Priority {

	private Priority() {
	}

	/** Every player in the game, the one with priority first. */
	public static List<Player> order(Game game) {
		List<Player> order = new ArrayList<>(game.getPlayersInGame());
		Optional<Space> antenna = antennaSpace(game.getBoard());
		if (antenna.isEmpty()) {
			order.sort(byUsername());
			return order;
		}
		Vector2D position = antenna.get().getPosition();
		Direction facing = antenna.get().getBoardElements().stream()
				.filter(PriorityAntenna.class::isInstance)
				.map(PriorityAntenna.class::cast).findFirst().orElseThrow()
				.getFacing();
		order.sort(Comparator
				.comparingInt((Player player) -> player.getRobot()
						.getCurrentSpace().distanceTo(position))
				.thenComparingDouble(
						player -> clockwiseAngle(position, facing, player))
				.thenComparing(byUsername()));
		return order;
	}

	private static Comparator<Player> byUsername() {
		return Comparator.comparing(player -> player.getUser().getUsername());
	}

	private static Optional<Space> antennaSpace(Board board) {
		return board.getSpaceMap().values().stream()
				.filter(space -> space.getBoardElements().stream()
						.anyMatch(PriorityAntenna.class::isInstance))
				.findFirst();
	}

	/**
	 * How far a line from the dish has to sweep clockwise to reach the robot,
	 * from 0 up to a full turn. Y grows downwards on the board, so a growing
	 * angle is clockwise.
	 */
	private static double clockwiseAngle(Vector2D antenna, Direction facing,
			Player player) {
		Vector2D robot = player.getRobot().getCurrentSpace();
		double toRobot = Math.atan2(robot.y() - antenna.y(),
				robot.x() - antenna.x());
		double dish = Math.atan2(facing.step().y(), facing.step().x());
		double angle = toRobot - dish;
		return angle < 0 ? angle + 2 * Math.PI : angle;
	}
}
