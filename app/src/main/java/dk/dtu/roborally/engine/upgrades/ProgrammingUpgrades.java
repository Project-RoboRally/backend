package dk.dtu.roborally.engine.upgrades;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.UpgradeKind;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.cards.DamageCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.PlayerMat;
import java.util.Arrays;
import java.util.List;

/**
 * Upgrades a player uses while programming, before their program is in. A
 * temporary upgrade is gone once used.
 *
 * @author Sebastian
 */
public final class ProgrammingUpgrades {

	private static final int HAND_SIZE = 9;
	private static final int SWAPPED_CARDS = 3;

	private ProgrammingUpgrades() {
	}

	/** Nine cards, or ten with a Memory Stick. */
	public static int handSize(Player player) {
		return player.getUpgrades().has(UpgradeType.MEMORY_STICK)
				? HAND_SIZE + 1
				: HAND_SIZE;
	}

	/**
	 * Once a round, one damage card from the hand goes back to its pile for
	 * good and a new card is drawn for it.
	 */
	private static void defrag(Game game, Player player, List<String> cards) {
		if (player.getUpgrades().usedThisRound(UpgradeType.DEFRAG_GIZMO)) {
			throw new IllegalStateException(
					"Defrag Gizmo was already used this round");
		}
		if (cards.size() != 1 || Arrays.stream(DamageInstruction.values())
				.noneMatch(damage -> damage.name().equals(cards.get(0)))) {
			throw new IllegalArgumentException("Pick one damage card");
		}
		DamageCard damage = (DamageCard) player.getPlayerMat()
				.removeFromHand(cards.get(0))
				.orElseThrow(() -> new IllegalArgumentException(
						"Not in your hand: " + cards.get(0)));
		game.getDamagePiles().giveBack(damage);
		player.getPlayerMat().drawCards(1);
		player.getUpgrades().markUsed(UpgradeType.DEFRAG_GIZMO);
	}

	public static void use(Game game, Player player, UpgradeType type,
			List<String> cards) {
		if (!player.getUpgrades().has(type)) {
			throw new IllegalArgumentException("Not installed: " + type);
		}
		PlayerMat mat = player.getPlayerMat();
		switch (type) {
			case RECHARGE -> player.addEnergy(3);
			case RECOMPILE -> {
				mat.discardHand();
				mat.drawHand(handSize(player));
			}
			case SPAM_BLOCKER -> {
				// New SPAM drawn here stays in the hand
				int blocked = 0;
				while (mat.removeFromHand(DamageInstruction.SPAM.name())
						.map(spam -> {
							game.getDamagePiles().giveBack((DamageCard) spam);
							return true;
						}).orElse(false)) {
					blocked++;
				}
				mat.drawCards(blocked);
			}
			case MEMORY_SWAP -> mat.swapCards(SWAPPED_CARDS);
			case CACHE_MEMORY -> mat.putOnTopOfDeck(cards);
			case DEFRAG_GIZMO -> defrag(game, player, cards);
			default -> throw new IllegalArgumentException(
					type + " can not be used while programming");
		}
		if (type.getKind() == UpgradeKind.TEMPORARY) {
			player.getUpgrades().remove(type);
		}
	}
}
