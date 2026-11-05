package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.LaserEmitter;
import dk.dtu.roborally.models.cards.DamageCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.PlayerMat;
import dk.dtu.roborally.models.player.Register;
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

	public static final String NONE = "NONE";

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
		Optional<Player> first = targets.stream().filter(
				target -> !game.getCurrentRound().isShielded(target.getId()))
				.findFirst();
		if (first.isPresent() && !pickWeapons(game, shooter, first.get())) {
			return false;
		}
		List<UpgradeType> picks = game.getCurrentRound().getWeaponPicks();
		for (Player target : targets) {
			hit(game, shooter, target, damage, picks);
		}
		if (first.isPresent()) {
			weaponEffects(game, shooter, first.get(), picks);
		}
		if (shooter.getUpgrades().has(UpgradeType.REAR_LASER)) {
			firstRobotHit(game, from, facing.uTurn()).ifPresent(
					target -> hit(game, shooter, target, damage, List.of()));
		}
		return true;
	}

	/**
	 * Asks the shooter which of their optional weapon upgrades to use on this
	 * shot, one at a time until they answer NONE. Returns false while waiting.
	 */
	private static boolean pickWeapons(Game game, Player shooter,
			Player target) {
		Round round = game.getCurrentRound();
		while (!round.isWeaponsPicked()) {
			List<UpgradeType> usable = usableWeapons(game, shooter, target)
					.stream()
					.filter(type -> !round.getWeaponPicks().contains(type))
					.toList();
			if (usable.isEmpty()) {
				round.setWeaponsPicked(true);
				break;
			}
			List<String> options = new ArrayList<>();
			options.add(NONE);
			usable.forEach(type -> options.add(type.name()));
			Optional<String> answer = round.ask(new PendingChoice(
					shooter.getId(), ChoiceType.WEAPON_UPGRADE, options));
			if (answer.isEmpty()) {
				return false;
			}
			if (answer.get().equals(NONE)) {
				round.setWeaponsPicked(true);
			} else {
				round.addWeaponPick(UpgradeType.valueOf(answer.get()));
			}
		}
		return true;
	}

	private static List<UpgradeType> usableWeapons(Game game, Player shooter,
			Player target) {
		boolean adjacent = adjacent(shooter, target);
		List<UpgradeType> usable = new ArrayList<>();
		if (shooter.getUpgrades().has(UpgradeType.MINI_HOWITZER)
				&& shooter.getEnergy() >= 1) {
			usable.add(UpgradeType.MINI_HOWITZER);
		}
		if (shooter.getUpgrades().has(UpgradeType.PRESSOR_BEAM)) {
			usable.add(UpgradeType.PRESSOR_BEAM);
		}
		if (shooter.getUpgrades().has(UpgradeType.TRACTOR_BEAM) && !adjacent) {
			usable.add(UpgradeType.TRACTOR_BEAM);
		}
		if (shooter.getUpgrades().has(UpgradeType.CORRUPTION_WAVE)) {
			usable.add(UpgradeType.CORRUPTION_WAVE);
		}
		if (shooter.getUpgrades().has(UpgradeType.BLUE_SCREEN_OF_DEATH)
				&& adjacent) {
			usable.add(UpgradeType.BLUE_SCREEN_OF_DEATH);
		}
		return usable;
	}

	/**
	 * What the picked upgrades do to the robot hit by the main laser. Mini
	 * Howitzer costs 1 energy for 2 more SPAM and a push away, Pressor Beam
	 * pushes it away and Tractor Beam pulls it closer.
	 */
	private static void weaponEffects(Game game, Player shooter, Player target,
			List<UpgradeType> picks) {
		Direction away = shooter.getRobot().getOrientation();
		if (picks.contains(UpgradeType.MINI_HOWITZER)
				&& shooter.getEnergy() >= 1) {
			shooter.spendEnergy(1);
			Damage.take(game, target, DamageInstruction.SPAM, 2);
			Movement.step(game, target, away);
		}
		if (picks.contains(UpgradeType.PRESSOR_BEAM)) {
			Movement.step(game, target, away);
		}
		if (picks.contains(UpgradeType.TRACTOR_BEAM)) {
			Movement.step(game, target, away.uTurn());
		}
	}

	/**
	 * Deals a shot's damage. A Deflector Shield stops it all. Blue Screen of
	 * Death makes one card a worm on an adjacent robot, otherwise a Trojan
	 * Needler makes it a trojan horse. Corruption Wave puts the SPAM on top of
	 * their deck, and a Scrambler swaps out the card in their next register.
	 */
	private static void hit(Game game, Player shooter, Player target,
			int damage, List<UpgradeType> picks) {
		if (game.getCurrentRound().isShielded(target.getId())) {
			return;
		}
		int spam = damage;
		if (picks.contains(UpgradeType.BLUE_SCREEN_OF_DEATH)
				&& adjacent(shooter, target)) {
			Damage.take(game, target, DamageInstruction.WORM, 1);
			spam--;
		} else if (shooter.getUpgrades().has(UpgradeType.TROJAN_NEEDLER)) {
			Damage.take(game, target, DamageInstruction.TROJAN_HORSE, 1);
			spam--;
		}
		if (picks.contains(UpgradeType.CORRUPTION_WAVE)) {
			Damage.onTopOfDeck(game, target, DamageInstruction.SPAM, spam);
		} else {
			Damage.take(game, target, DamageInstruction.SPAM, spam);
		}
		if (shooter.getUpgrades().has(UpgradeType.SCRAMBLER)) {
			scramble(game, target);
		}
	}

	/**
	 * The card in the target's next register is swapped for the top card of
	 * their deck. Nothing happens in the last register.
	 */
	private static void scramble(Game game, Player target) {
		int next = game.getCurrentRound().getCurrentRegisterIndex().orElse(0)
				+ 1;
		if (next >= PlayerMat.REGISTER_COUNT) {
			return;
		}
		Register register = target.getPlayerMat().getRegisters().get(next);
		if (register.getCard() == null) {
			return;
		}
		if (register.getCard() instanceof DamageCard damage) {
			game.getDamagePiles().giveBack(damage);
		} else {
			target.getPlayerMat().addToDiscardPile(register.getCard());
		}
		register.setCard(target.getPlayerMat().drawTop().orElse(null));
	}

	private static boolean adjacent(Player one, Player other) {
		return one.getRobot().getCurrentSpace()
				.distanceTo(other.getRobot().getCurrentSpace()) == 1;
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
