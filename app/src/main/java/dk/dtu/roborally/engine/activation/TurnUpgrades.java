package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.player.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Temporary upgrades a player can use on their turn, right before their card is
 * played. The player is asked with a TURN_UPGRADE choice until they say NONE.
 * Upgrades that need a direction ask for it right after.
 *
 * @author Sebastian
 */
public final class TurnUpgrades {

	public static final String NONE = "NONE";

	/** In the order they are offered. */
	private static final List<UpgradeType> ON_YOUR_TURN = List
			.of(UpgradeType.RECHARGE, UpgradeType.ZOOP, UpgradeType.BOINK);

	private TurnUpgrades() {
	}

	/**
	 * Asks the player about their turn upgrades and uses the ones they pick.
	 * Returns false while waiting on an answer.
	 */
	static boolean ask(Game game, Player player) {
		Round round = game.getCurrentRound();
		while (!round.isTurnDone()) {
			Optional<UpgradeType> picked = round.getPickedUpgrade();
			if (picked.isPresent()) {
				Optional<String> direction = round.ask(new PendingChoice(
						player.getId(), followUp(picked.get()), directions()));
				if (direction.isEmpty()) {
					return false;
				}
				use(game, player, picked.get(), direction.get());
				round.setPickedUpgrade(null);
				continue;
			}
			List<UpgradeType> usable = usable(player);
			if (usable.isEmpty()) {
				round.setTurnDone(true);
				break;
			}
			List<String> options = new ArrayList<>();
			options.add(NONE);
			usable.forEach(type -> options.add(type.name()));
			Optional<String> answer = round.ask(new PendingChoice(
					player.getId(), ChoiceType.TURN_UPGRADE, options));
			if (answer.isEmpty()) {
				return false;
			}
			if (answer.get().equals(NONE)) {
				round.setTurnDone(true);
				continue;
			}
			UpgradeType type = UpgradeType.valueOf(answer.get());
			if (needsDirection(type)) {
				round.setPickedUpgrade(type);
			} else {
				use(game, player, type, null);
			}
		}
		return true;
	}

	private static List<UpgradeType> usable(Player player) {
		if (player.getRobot().isRebootedThisRound()) {
			// Rebooted robots can not use upgrades
			return List.of();
		}
		return ON_YOUR_TURN.stream()
				.filter(type -> player.getUpgrades().has(type)).toList();
	}

	private static boolean needsDirection(UpgradeType type) {
		return type == UpgradeType.ZOOP || type == UpgradeType.BOINK;
	}

	private static ChoiceType followUp(UpgradeType type) {
		return type == UpgradeType.ZOOP
				? ChoiceType.ZOOP_FACING
				: ChoiceType.BOINK_DIRECTION;
	}

	private static List<String> directions() {
		return Arrays.stream(Direction.values()).map(Direction::name).toList();
	}

	private static void use(Game game, Player player, UpgradeType type,
			String direction) {
		switch (type) {
			case RECHARGE -> player.addEnergy(3);
			case ZOOP -> player.getRobot().face(Direction.valueOf(direction));
			// Boinking does not turn the robot
			case BOINK ->
				Movement.step(game, player, Direction.valueOf(direction));
			default -> throw new IllegalArgumentException(
					type + " is not used on your turn");
		}
		player.getUpgrades().remove(type);
		if (type != UpgradeType.RECHARGE) {
			game.getCurrentRound().getLog()
					.addStep(BoardElements.snapshot(game));
		}
	}
}
