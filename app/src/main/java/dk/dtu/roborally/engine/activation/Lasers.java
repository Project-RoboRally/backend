package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.LaserEmitter;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fires the lasers after the board elements. A laser hits the first robot in
 * its line and goes no further. Walls and the priority antenna stop it.
 *
 * @author Sebastian
 */
public final class Lasers {

	private Lasers() {
	}

	public static void fire(Game game) {
		fireBoardLasers(game);
		fireRobotLasers(game);
	}

	/**
	 * Board lasers shoot from their emitter, so a robot standing on the
	 * emitter's space gets hit first. Each beam deals one SPAM.
	 */
	private static void fireBoardLasers(Game game) {
		for (LaserEmitter laser : game.getBoard()
				.getElements(LaserEmitter.class)) {
			Optional<Player> hit = Movement.robotAt(game, laser.getPosition())
					.or(() -> firstRobotHit(game, laser.getPosition(),
							laser.getDirection()));
			hit.ifPresent(player -> Damage.take(game, player,
					DamageInstruction.SPAM, laser.getDamage()));
		}
	}

	/**
	 * Every robot shoots straight ahead and the first robot in line takes one
	 * SPAM. They all fire at once, so who shoots first does not matter. A robot
	 * that rebooted this round can be hit but can not shoot.
	 */
	private static void fireRobotLasers(Game game) {
		List<Player> hits = new ArrayList<>();
		for (Player shooter : game.getPlayersInGame()) {
			if (shooter.getRobot().isRebootedThisRound()) {
				continue;
			}
			firstRobotHit(game, shooter.getRobot().getCurrentSpace(),
					shooter.getRobot().getOrientation()).ifPresent(hits::add);
		}
		hits.forEach(
				target -> Damage.take(game, target, DamageInstruction.SPAM, 1));
	}

	/**
	 * The first robot in a straight line from the given space, not counting the
	 * space itself. Empty if the line hits a wall, the antenna or the edge of
	 * the board first.
	 */
	static Optional<Player> firstRobotHit(Game game, Vector2D from,
			Direction direction) {
		Board board = game.getBoard();
		Vector2D position = from;
		while (true) {
			Vector2D next = position.add(direction.step());
			if (!board.isWithinBoard(next)
					|| Movement.wallBlocks(board, position, next, direction)
					|| Movement.hasAntenna(board.getSpace(next))) {
				return Optional.empty();
			}
			Optional<Player> robot = Movement.robotAt(game, next);
			if (robot.isPresent()) {
				return robot;
			}
			position = next;
		}
	}
}
