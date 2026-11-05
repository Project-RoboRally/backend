package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;

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
	}
}
