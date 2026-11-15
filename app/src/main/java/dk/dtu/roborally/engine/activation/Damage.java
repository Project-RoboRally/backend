package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.models.cards.DamageCard;
import dk.dtu.roborally.models.game.DamagePiles;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.QueuedDamage;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.player.Player;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Hands out damage cards. They are drawn one at a time from the damage piles
 * and go to the player's discard pile, so they show up in later hands. When a
 * pile is empty the player picks another type.
 *
 * @author Sebastian, Elias
 */
public final class Damage {

	private Damage() {
	}

	/** Same as take, but the cards go on top of the deck instead. */
	public static void onTopOfDeck(Game game, Player player,
			DamageInstruction type, int amount) {
		deal(game, player, type, amount, true);
	}

	public static void take(Game game, Player player, DamageInstruction type,
			int amount) {
		deal(game, player, type, amount, false);
	}

	/**
	 * Lets players pick another damage type for cards queued because a pile ran
	 * out. Returns false while waiting for an answer.
	 */
	public static boolean giveQueued(Game game) {
		Round round = game.getCurrentRound();
		while (!round.getDamageDraws().isEmpty()) {
			QueuedDamage draw = round.getDamageDraws().get(0);
			List<String> options = game.getDamagePiles().typesLeft().stream()
					.map(Enum::name).toList();
			if (options.isEmpty()) {
				round.removeFirstDamageDraw();
				continue;
			}
			String chosen;
			if (options.size() == 1) {
				chosen = options.get(0);
			} else {
				Optional<String> answer = round.ask(new PendingChoice(
						draw.playerId(), ChoiceType.DAMAGE_TYPE, options));
				if (answer.isEmpty()) {
					return false;
				}
				chosen = answer.get();
			}
			give(game, draw, DamageInstruction.valueOf(chosen));
			round.removeFirstDamageDraw();
		}
		return true;
	}

	private static void deal(Game game, Player player, DamageInstruction type,
			int amount, boolean onTopOfDeck) {
		for (int i = 0; i < amount; i++) {
			draw(game, player, type, onTopOfDeck).ifPresent(card -> give(player,
					card, onTopOfDeck));
		}
	}

	private static Optional<DamageCard> draw(Game game, Player player,
			DamageInstruction type, boolean onTopOfDeck) {
		DamagePiles piles = game.getDamagePiles();
		Optional<DamageCard> card = piles.take(type);
		if (card.isPresent()) {
			return card;
		}
		List<DamageInstruction> other = piles.typesLeft();
		if (other.isEmpty()) {
			return Optional.empty();
		}
		Round round = game.getCurrentRound();
		if (!round.isActivating()) {
			DamageInstruction fallback = other.stream()
					.max(Comparator.comparingInt(piles::size)).orElseThrow();
			return piles.take(fallback);
		}
		round.addDamageDraw(player.getId(), onTopOfDeck);
		return Optional.empty();
	}

	private static void give(Game game, QueuedDamage draw,
			DamageInstruction type) {
		Player player = game.getPlayersInGame().stream()
				.filter(candidate -> candidate.getId().equals(draw.playerId()))
				.findFirst().orElseThrow();
		game.getDamagePiles().take(type)
				.ifPresent(card -> give(player, card, draw.onTopOfDeck()));
	}

	private static void give(Player player, DamageCard card,
			boolean onTopOfDeck) {
		if (onTopOfDeck) {
			player.getPlayerMat().addToTopOfDeck(card);
		} else {
			player.getPlayerMat().addToDiscardPile(card);
		}
	}
}
