package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.RotationDirection;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.elements.Gear;
import dk.dtu.roborally.models.game.ActivationLog;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.RobotView;
import java.util.List;
import java.util.Optional;

/**
 * Activates the board elements after every robot has played its card for a
 * register. They go in the rulebook's order and only affect robots standing on
 * them at that moment.
 *
 * @author Sebastian
 */
public final class BoardElements {

	private BoardElements() {
	}

	public static void activate(Game game, int register, ActivationLog log) {
		rotateOnGears(game, log);
	}

	/** Red gears turn robots left, green gears turn them right. */
	private static void rotateOnGears(Game game, ActivationLog log) {
		boolean turned = false;
		for (Player player : game.getPlayersInGame()) {
			Optional<Gear> gear = element(game, player, Gear.class);
			if (gear.isEmpty()) {
				continue;
			}
			if (gear.get()
					.getRotationDirection() == RotationDirection.CLOCKWISE) {
				player.getRobot().turnRight();
			} else {
				player.getRobot().turnLeft();
			}
			turned = true;
		}
		if (turned) {
			log.addStep(snapshot(game));
		}
	}

	/** The element of the given type on the space the robot stands on. */
	static <T> Optional<T> element(Game game, Player player, Class<T> type) {
		Space space = game.getBoard()
				.getSpace(player.getRobot().getCurrentSpace());
		if (space == null) {
			return Optional.empty();
		}
		return space.getBoardElements().stream().filter(type::isInstance)
				.map(type::cast).findFirst();
	}

	static List<RobotView> snapshot(Game game) {
		return game.getPlayersInGame().stream().map(RobotView::from).toList();
	}
}
