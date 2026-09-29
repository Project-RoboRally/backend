package dk.dtu.roborally.models.cards;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a deck of cards. This can be used to hold both programmable cards
 * or upgrade cards. It models the game necessary behavior of a physical
 * carddeck.
 *
 * @author August
 */

public class CardDeck<T extends Card> {
	private final List<T> cards = new ArrayList<>();

	public CardDeck() {
	}

	public void addCard(T card) {
		cards.add(Objects.requireNonNull(card));
	}

	public Optional<T> drawCard() {
		if (cards.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(cards.remove(cards.size() - 1));
	}

	public void shuffle() {
		Collections.shuffle(cards);
	}

	public int size() {
		return cards.size();
	}

	public boolean isEmpty() {
		return cards.isEmpty();
	}
}
