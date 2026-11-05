package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.player.Player;
import java.util.List;
import java.util.Optional;

/**
 * Upgrades that do something when your robot pushes another one with its card.
 * Pushing the same robot several spaces with one card counts as one push.
 *
 * @author Sebastian
 */
public final class PushUpgrades {

	private PushUpgrades() {
	}

	/**
	 * Ramming Gear deals a SPAM. That card becomes a worm with Blue Screen of
	 * Death, or a trojan horse with a Trojan Needler. Virus Module also gives a
	 * virus.
	 */
	static void pushed(Game game, Player pusher, Player target) {
		if (pusher.getRobot().isRebootedThisRound()) {
			// Rebooted robots can not use upgrades
			return;
		}
		if (pusher.getUpgrades().has(UpgradeType.RAMMING_GEAR)) {
			DamageInstruction damage = DamageInstruction.SPAM;
			if (pusher.getUpgrades().has(UpgradeType.BLUE_SCREEN_OF_DEATH)) {
				damage = DamageInstruction.WORM;
			} else if (pusher.getUpgrades().has(UpgradeType.TROJAN_NEEDLER)) {
				damage = DamageInstruction.TROJAN_HORSE;
			}
			Damage.take(game, target, damage, 1);
		}
		if (pusher.getUpgrades().has(UpgradeType.VIRUS_MODULE)) {
			Damage.take(game, target, DamageInstruction.VIRUS, 1);
		}
		if (pusher.getUpgrades().has(UpgradeType.MODULAR_CHASSIS)
				&& !target.getUpgrades().getAll().isEmpty()) {
			game.getCurrentRound().addChassisSwap(pusher.getId(),
					target.getId());
		}
	}

	/**
	 * Modular Chassis: after the card, the pusher picks one of the pushed
	 * robot's upgrades to take, and gives it the Modular Chassis. Returns false
	 * while waiting for the pick.
	 */
	static boolean swapChassis(Game game) {
		Round round = game.getCurrentRound();
		for (List<String> swap : round.getChassisSwaps()) {
			Player pusher = find(game, swap.get(0));
			Player target = find(game, swap.get(1));
			if (pusher.getUpgrades().has(UpgradeType.MODULAR_CHASSIS)
					&& !target.getUpgrades().getAll().isEmpty()) {
				Optional<String> taken = round.ask(new PendingChoice(
						pusher.getId(), ChoiceType.MODULAR_CHASSIS,
						target.getUpgrades().getAll().stream()
								.map(UpgradeType::name).toList()));
				if (taken.isEmpty()) {
					return false;
				}
				UpgradeType type = UpgradeType.valueOf(taken.get());
				target.getUpgrades().remove(type);
				pusher.getUpgrades().remove(UpgradeType.MODULAR_CHASSIS);
				pusher.getUpgrades().give(type);
				target.getUpgrades().give(UpgradeType.MODULAR_CHASSIS);
			}
			round.removeChassisSwap(swap);
		}
		return true;
	}

	private static Player find(Game game, String playerId) {
		return game.getPlayersInGame().stream()
				.filter(player -> player.getId().equals(playerId)).findFirst()
				.orElseThrow();
	}
}
