package dk.dtu.roborally.models.cards;

import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.DamageInstruction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardDeckTest {
	private CardDeck<ProgrammableCard> deck;

	@BeforeEach
	void setUp() {
		deck = new CardDeck<>();
	}

	@Test
	void drawingFromEmptyDeckReturnsEmpty() {
		assertTrue(deck.drawCard().isEmpty());
		assertEquals(0, deck.size());
	}

	@Test
	void drawingReturnsAndRemovesTopCard() {
		ProgrammingCard first = new ProgrammingCard(CommandInstruction.MOVE_1);
		DamageCard second = new DamageCard(DamageInstruction.SPAM);

		deck.addCard(first);
		deck.addCard(second);

		assertSame(second, deck.drawCard().orElseThrow());
		assertEquals(1, deck.size());

		assertSame(first, deck.drawCard().orElseThrow());
		assertTrue(deck.isEmpty());
		assertTrue(deck.drawCard().isEmpty());
	}

	@Test
	void nullCardIsRejectedWithoutChangingDeck() {
		ProgrammingCard card = new ProgrammingCard(CommandInstruction.MOVE_1);
		deck.addCard(card);

		assertThrows(NullPointerException.class, () -> deck.addCard(null));

		assertEquals(1, deck.size());
		assertSame(card, deck.drawCard().orElseThrow());
	}

	@Test
	void shufflingPreservesEveryCardExactlyOnce() {
		// Two separate cards may carry the same instruction.
		List<ProgrammableCard> expected = List.of(
				new ProgrammingCard(CommandInstruction.MOVE_1),
				new ProgrammingCard(CommandInstruction.MOVE_1),
				new ProgrammingCard(CommandInstruction.LEFT_TURN),
				new DamageCard(DamageInstruction.SPAM));

		for (ProgrammableCard card : expected) {
			deck.addCard(card);
		}

		deck.shuffle();

		assertEquals(expected.size(), deck.size());

		List<ProgrammableCard> remaining = new ArrayList<>(expected);

		for (int i = 0; i < expected.size(); i++) {
			ProgrammableCard drawn = deck.drawCard().orElseThrow();

			assertTrue(remaining.remove(drawn),
					"Drawn card was unexpected or already drawn");
		}

		assertTrue(remaining.isEmpty());
		assertTrue(deck.drawCard().isEmpty());
	}

	@Test
	void shufflingEmptyDeckIsHarmless() {
		assertDoesNotThrow(() -> deck.shuffle());
		assertTrue(deck.drawCard().isEmpty());
	}
}
