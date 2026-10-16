
package dk.dtu.roborally.engine.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import dk.dtu.roborally.models.cards.Card;
import dk.dtu.roborally.models.cards.CardDeck;
import dk.dtu.roborally.models.player.Player;

/**
 * Represents the programming cards held by a player and manages their selection
 * and assignment to registers during the programming phase.
 *
 * @author Sebastian, Kerem
 */
public class ProgrammingService {

	// Hard coded values from the ruleset of Robo Rally
	private static final int HAND_SIZE = 9;
	private static final int REGISTER_COUNT = 5;

	private final Card[] programmingCards = new Card[HAND_SIZE];
	private final Card[] selectedProgrammingCards = new Card[REGISTER_COUNT];

	public final Player player;

	private int cardCount = 0;
	private boolean submitted = false;

	public ProgrammingService(Player player) {
		this.player = Objects.requireNonNull(player);
	}

	/**
	 * Draws cards until the hand contains 9 cards or the deck is empty. Call
	 * when a new programming phase begins.
	 */
	public void beginProgrammingPhase(CardDeck<Card> deck) {
		Objects.requireNonNull(deck);
		ensureNotSubmitted();

		Arrays.fill(selectedProgrammingCards, null);

		while (cardCount < HAND_SIZE && !deck.isEmpty()) {
			Card card = deck.drawCard().orElseThrow(
					() -> new IllegalStateException("Deck unexpectedly empty"));

			programmingCards[cardCount++] = card;
		}
	}

	public Card[] getCards() {
		return Arrays.copyOf(programmingCards, cardCount);
	}

	public Card[] getAvailableCards() {
		List<Card> available = new ArrayList<>();

		for (int i = 0; i < cardCount; i++) {
			if (!isSelected(programmingCards[i])) {
				available.add(programmingCards[i]);
			}
		}

		return available.toArray(new Card[0]);
	}

	/**
	 * Assigns a card to a register. Registers are indexed from 0 to 4.
	 * Assigning another card to an occupied register replaces it.
	 */
	public void selectCard(int register, Card card) {
		ensureNotSubmitted();
		checkRegister(register);
		Objects.requireNonNull(card, "Card cannot be null");

		if (!containsCard(card)) {
			throw new IllegalArgumentException(
					"Card is not in the player's hand");
		}

		for (int i = 0; i < REGISTER_COUNT; i++) {
			if (i != register && selectedProgrammingCards[i] == card) {
				throw new IllegalArgumentException(
						"Card is already assigned to another register");
			}
		}

		selectedProgrammingCards[register] = card;
	}

	public void clearRegister(int register) {
		ensureNotSubmitted();
		checkRegister(register);

		selectedProgrammingCards[register] = null;
	}

	public Card[] getSelectedProgrammingCards() {
		return Arrays.copyOf(selectedProgrammingCards, REGISTER_COUNT);
	}

	public Card[] submitProgram() {
		ensureNotSubmitted();

		for (int i = 0; i < REGISTER_COUNT; i++) {
			if (selectedProgrammingCards[i] == null) {
				throw new IllegalStateException(
						"Cannot submit: register " + (i + 1) + " is empty");
			}
		}

		submitted = true;
		return getSelectedProgrammingCards();
	}

	public void finishProgrammingPhase() {
		ensureSubmitted();

		for (Card selected : selectedProgrammingCards) {
			for (int i = 0; i < cardCount; i++) {
				if (programmingCards[i] == selected) {
					for (int j = i; j < cardCount - 1; j++) {
						programmingCards[j] = programmingCards[j + 1];
					}

					programmingCards[--cardCount] = null;
					break;
				}
			}
		}

		Arrays.fill(selectedProgrammingCards, null);
		submitted = false;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	private boolean containsCard(Card card) {
		for (int i = 0; i < cardCount; i++) {
			if (programmingCards[i] == card) {
				return true;
			}
		}

		return false;
	}

	private boolean isSelected(Card card) {
		for (Card selected : selectedProgrammingCards) {
			if (selected == card) {
				return true;
			}
		}

		return false;
	}

	private void ensureNotSubmitted() {
		if (submitted) {
			throw new IllegalStateException(
					"Program has already been submitted");
		}
	}

	private void ensureSubmitted() {
		if (!submitted) {
			throw new IllegalStateException("Program has not been submitted");
		}
	}

	private void checkRegister(int register) {
		if (register < 0 || register >= REGISTER_COUNT) {
			throw new IndexOutOfBoundsException(
					"Register must be between 0 and 4");
		}
	}
}
