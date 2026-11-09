package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;

/**
 * Hands out damage cards. They are drawn one at a time from the damage piles
 * and go to the player's discard pile, so they show up in later hands.
 *
 * @author Sebastian
 */
public final class Damage {

	private Damage() {
	}

	/** Same as take, but the cards go on top of the deck instead. */
	public static void onTopOfDeck(Game game, Player player,
			DamageInstruction type, int amount) {
		for (int i = 0; i < amount; i++) {
			game.getDamagePiles().take(type)
					.ifPresent(player.getPlayerMat()::addToTopOfDeck);
		}
	}

	public static void take(Game game, Player player, DamageInstruction type,
			int amount) {
		for (int i = 0; i < amount; i++) {
			game.getDamagePiles().take(type)
					.ifPresent(player.getPlayerMat()::addToDiscardPile);
		}
	}
}
