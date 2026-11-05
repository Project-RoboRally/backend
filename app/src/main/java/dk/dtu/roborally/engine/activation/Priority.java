package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.PriorityAntenna;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Works out whose turn it is. The robot closest to the priority antenna goes
 * first, with username breaking ties.
 *
 * @author Sebastian
 */
public final class Priority {

	private Priority() {
	}

	/** Every player in the game, the one with priority first. */
	public static List<Player> order(Game game) {
		List<Player> order = new ArrayList<>(game.getPlayersInGame());
		order.sort(activationOrder(antennaPosition(game.getBoard())));
		return order;
	}

	private static Comparator<Player> activationOrder(Vector2D antenna) {
		return Comparator
				.comparingInt(
						(Player player) -> distanceToAntenna(player, antenna))
				.thenComparing(player -> player.getUser().getUsername());
	}

	private static Vector2D antennaPosition(Board board) {
		return board.getSpaceMap().values().stream()
				.filter(space -> space.getBoardElements().stream()
						.anyMatch(PriorityAntenna.class::isInstance))
				.map(Space::getPosition).findFirst().orElse(null);
	}

	/**
	 * Manhattan distance to the antenna. With no antenna on the board every
	 * robot is equally close, so username decides the order.
	 */
	private static int distanceToAntenna(Player player, Vector2D antenna) {
		if (antenna == null) {
			return 0;
		}
		return player.getRobot().getCurrentSpace().distanceTo(antenna);
	}
}
