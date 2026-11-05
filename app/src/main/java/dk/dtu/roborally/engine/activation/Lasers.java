package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.LaserEmitter;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fires the lasers after the board elements. A laser hits the first robot in
 * its line and goes no further. Walls and the priority antenna stop it, and a
 * Deflector Shield stops robot lasers.
 *
 * @author Sebastian
 */
public final class Lasers {

	private Lasers() {
	}

	/**
	 * Fires the board lasers, then every robot's weapons one robot at a time in
	 * this register's order. Returns false while a robot waits on its player,
	 * the shots fired so far are kept on the round.
	 */
	public static boolean fire(Game game) {
		Round round = game.getCurrentRound();
		if (!round.isBoardLasersFired()) {
			fireBoardLasers(game);
			round.setBoardLasersFired(true);
		}
		List<String> order = round.getActivationOrder();
		while (round.getShotsFired() < order.size()) {
			Player shooter = game.getPlayersInGame().stream()
					.filter(player -> player.getId()
							.equals(order.get(round.getShotsFired())))
					.findFirst().orElseThrow();
			if (!shoot(game, shooter)) {
				return false;
			}
			round.markShotFired();
		}
		return true;
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
	 * A robot shoots straight ahead and the first robot in line takes one SPAM.
	 * A Double-Barrel Laser adds one, a Rail Gun hits every robot in line
	 * through walls, and a Rear Laser shoots backwards too. A robot that
	 * rebooted this round can be hit but can not shoot.
	 */
	private static boolean shoot(Game game, Player shooter) {
		if (shooter.getRobot().isRebootedThisRound()) {
			return true;
		}
		Vector2D from = shooter.getRobot().getCurrentSpace();
		Direction facing = shooter.getRobot().getOrientation();
		int damage = shooter.getUpgrades().has(UpgradeType.DOUBLE_BARREL_LASER)
				? 2
				: 1;
		List<Player> targets = shooter.getUpgrades().has(UpgradeType.RAIL_GUN)
				? everyRobotInLine(game, from, facing)
				: firstRobotHit(game, from, facing).stream().toList();
		for (Player target : targets) {
			hit(game, target, damage);
		}
		if (shooter.getUpgrades().has(UpgradeType.REAR_LASER)) {
			firstRobotHit(game, from, facing.uTurn())
					.ifPresent(target -> hit(game, target, damage));
		}
		return true;
	}

	/** A Deflector Shield stops robot lasers. */
	private static void hit(Game game, Player target, int damage) {
		if (!game.getCurrentRound().isShielded(target.getId())) {
			Damage.take(game, target, DamageInstruction.SPAM, damage);
		}
	}

	/**
	 * Every robot in a straight line from the space, through walls and other
	 * robots, until the antenna or the edge of the board.
	 */
	private static List<Player> everyRobotInLine(Game game, Vector2D from,
			Direction direction) {
		List<Player> robots = new ArrayList<>();
		Vector2D position = from.add(direction.step());
		while (game.getBoard().isWithinBoard(position)
				&& !Movement.hasAntenna(game.getBoard().getSpace(position))) {
			Movement.robotAt(game, position).ifPresent(robots::add);
			position = position.add(direction.step());
		}
		return robots;
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
