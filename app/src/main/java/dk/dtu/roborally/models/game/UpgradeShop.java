package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.cards.CardDeck;
import dk.dtu.roborally.models.cards.UpgradeCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The cards for sale in the upgrade phase. Holds one card per player. We only
 * have a few upgrades so far, so unlike the rulebook used cards are shuffled
 * back in when the deck runs out, otherwise the shop would empty.
 *
 * @author Sebastian
 */
public class UpgradeShop {

	private static final int COPIES_OF_EACH_UPGRADE = 6;

	private final CardDeck<UpgradeCard> deck = new CardDeck<>();
	private final List<UpgradeCard> discardPile = new ArrayList<>();
	private final List<UpgradeCard> cards = new ArrayList<>();

	public UpgradeShop() {
		for (UpgradeType type : UpgradeType.values()) {
			for (int i = 0; i < COPIES_OF_EACH_UPGRADE; i++) {
				deck.addCard(new UpgradeCard(type));
			}
		}
		deck.shuffle();
	}

	/**
	 * Fills the shop up to the given size. A shop that is already full means
	 * nobody bought anything last round, so all of it is replaced.
	 */
	public void refill(int size) {
		if (cards.size() == size) {
			discardPile.addAll(cards);
			cards.clear();
		}
		while (cards.size() < size) {
			if (deck.isEmpty()) {
				shuffleDiscardPileIntoDeck();
			}
			Optional<UpgradeCard> card = deck.drawCard();
			if (card.isEmpty()) {
				return;
			}
			cards.add(card.get());
		}
	}

	/** Takes a card of the given type out of the shop. */
	public UpgradeCard buy(UpgradeType type) {
		for (UpgradeCard card : cards) {
			if (card.getType() == type) {
				cards.remove(card);
				discardPile.add(card);
				return card;
			}
		}
		throw new IllegalArgumentException(
				"Upgrade is not in the shop: " + type);
	}

	public List<UpgradeCard> getCards() {
		return Collections.unmodifiableList(cards);
	}

	public int getDeckSize() {
		return deck.size();
	}

	private void shuffleDiscardPileIntoDeck() {
		discardPile.forEach(deck::addCard);
		discardPile.clear();
		deck.shuffle();
	}
}
