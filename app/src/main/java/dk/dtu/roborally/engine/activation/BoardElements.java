package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.ConveyorBeltType;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.RotationDirection;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.ConveyorBelt;
import dk.dtu.roborally.models.board.elements.EnergySpace;
import dk.dtu.roborally.models.board.elements.Gear;
import dk.dtu.roborally.models.board.elements.PushPanel;
import dk.dtu.roborally.models.game.ActivationLog;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.PlayerMat;
import dk.dtu.roborally.models.player.RobotView;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
		List<Player> onBlue = convey(game, ConveyorBeltType.BLUE,
				game.getPlayersInGame(), log);
		// Blue belts carry robots a second space if they are still on one
		convey(game, ConveyorBeltType.BLUE, onBlue, log);
		convey(game, ConveyorBeltType.GREEN, game.getPlayersInGame(), log);
		pushFromPanels(game, register, log);
		rotateOnGears(game, log);
	}

	/**
	 * Moves every robot on a belt of the given colour one space, all at the
	 * same time. Belts never push: a robot stays put if the space ahead has a
	 * robot that is not moving away, if another robot is heading for the same
	 * space, or if a wall is in the way. Returns the robots that were on a belt
	 * of that colour.
	 */
	private static List<Player> convey(Game game, ConveyorBeltType colour,
			Collection<Player> candidates, ActivationLog log) {
		Map<Player, ConveyorBelt> riding = new LinkedHashMap<>();
		for (Player player : candidates) {
			element(game, player, ConveyorBelt.class)
					.filter(belt -> belt.getType() == colour)
					.ifPresent(belt -> riding.put(player, belt));
		}
		Map<Player, Vector2D> targets = new HashMap<>();
		Set<Player> stuck = new HashSet<>();
		for (Map.Entry<Player, ConveyorBelt> ride : riding.entrySet()) {
			Vector2D from = ride.getKey().getRobot().getCurrentSpace();
			Direction direction = ride.getValue().getDirection();
			Vector2D to = from.add(direction.step());
			targets.put(ride.getKey(), to);
			if (Movement.blocked(game.getBoard(), from, to, direction)) {
				stuck.add(ride.getKey());
			}
		}
		for (Player player : riding.keySet()) {
			long sameTarget = targets.values().stream()
					.filter(targets.get(player)::equals).count();
			if (sameTarget > 1) {
				stuck.add(player);
			}
		}
		// A robot that is not moving out of the way stops the one behind it,
		// which can stop the one behind that, and so on
		boolean changed = true;
		while (changed) {
			changed = false;
			for (Player player : riding.keySet()) {
				if (stuck.contains(player)) {
					continue;
				}
				Optional<Player> ahead = Movement.robotAt(game,
						targets.get(player));
				if (ahead.isPresent() && (!riding.containsKey(ahead.get())
						|| stuck.contains(ahead.get()))) {
					stuck.add(player);
					changed = true;
				}
			}
		}

		boolean moved = false;
		for (Player player : riding.keySet()) {
			if (stuck.contains(player)) {
				continue;
			}
			Vector2D from = player.getRobot().getCurrentSpace();
			Direction direction = riding.get(player).getDirection();
			player.getRobot().moveTo(targets.get(player));
			if (!Movement.fallIfNeeded(game, player, from)) {
				turnOntoCurve(game, player, direction);
			}
			moved = true;
		}
		if (moved) {
			log.addStep(snapshot(game));
		}
		return List.copyOf(riding.keySet());
	}

	/**
	 * A belt carrying a robot onto a belt pointing another way is a curve, and
	 * the robot turns with it.
	 */
	private static void turnOntoCurve(Game game, Player player,
			Direction travelled) {
		element(game, player, ConveyorBelt.class).ifPresent(belt -> {
			if (belt.getDirection() == travelled.turnRight()) {
				player.getRobot().turnRight();
			} else if (belt.getDirection() == travelled.turnLeft()) {
				player.getRobot().turnLeft();
			}
		});
	}

	/**
	 * Push panels push the robot on them one space away from the wall, but only
	 * in the registers printed on the panel. A robot in the way gets pushed
	 * along too.
	 */
	private static void pushFromPanels(Game game, int register,
			ActivationLog log) {
		boolean pushed = false;
		for (Player player : Priority.order(game)) {
			Optional<PushPanel> panel = element(game, player, PushPanel.class)
					.filter(found -> found.pushesIn(register));
			if (panel.isPresent()) {
				Movement.step(game, player, panel.get().getPushDirection());
				pushed = true;
			}
		}
		if (pushed) {
			log.addStep(snapshot(game));
		}
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

	/**
	 * Robots ending the register on an energy space take its cube, if it still
	 * has one. Once taken the space stays empty, except that ending the fifth
	 * register on it gives a cube from the bank anyway.
	 */
	public static void collectEnergy(Game game, int register) {
		boolean lastRegister = register == PlayerMat.REGISTER_COUNT - 1;
		for (Player player : Priority.order(game)) {
			Optional<EnergySpace> space = element(game, player,
					EnergySpace.class);
			if (space.isEmpty()) {
				continue;
			}
			if (space.get().getCubeAmount() > 0) {
				space.get().setCubeAmount(space.get().getCubeAmount() - 1);
				player.addEnergy(1);
			} else if (lastRegister) {
				player.addEnergy(1);
			}
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
