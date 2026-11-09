package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.cards.CardDeck;
import dk.dtu.roborally.models.cards.UpgradeCard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The cards for sale in the upgrade phase. Holds one card per player, from a
 * deck with one copy of every upgrade. Cards nobody bought are removed from
 * play when the shop is replaced, so the deck can run out.
 *
 * @author Sebastian, Elias
 */
public class UpgradeShop {

	private final CardDeck<UpgradeCard> deck = new CardDeck<>();
	private final List<UpgradeCard> cards = new ArrayList<>();

	public UpgradeShop() {
		for (UpgradeType type : UpgradeType.values()) {
			deck.addCard(new UpgradeCard(type));
		}
		deck.shuffle();
	}

	/**
	 * Fills the shop up to the given size. A shop that is already full means
	 * nobody bought anything last round, so all of it is removed from play and
	 * replaced. With the deck empty the shop just holds fewer cards.
	 */
	public void refill(int size) {
		if (cards.size() == size) {
			cards.clear();
		}
		while (cards.size() < size) {
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
				return card;
			}
		}
		throw new IllegalArgumentException(
				"Upgrade is not in the shop: " + type);
	}

	public List<UpgradeCard> getCards() {
		return Collections.unmodifiableList(cards);
	}

	/** Deck from bottom to top. The last card is drawn next. */
	public List<UpgradeType> getDeckTypes() {
		return deck.getCards().stream().map(UpgradeCard::getType).toList();
	}

	/** Replaces the deck and the cards for sale. */
	public void restore(List<UpgradeType> deckTypes,
			List<UpgradeType> forSale) {
		deck.clear();
		for (UpgradeType type : deckTypes) {
			deck.addCard(new UpgradeCard(type));
		}
		cards.clear();
		for (UpgradeType type : forSale) {
			cards.add(new UpgradeCard(type));
		}
	}

	public int getDeckSize() {
		return deck.size();
	}
}
