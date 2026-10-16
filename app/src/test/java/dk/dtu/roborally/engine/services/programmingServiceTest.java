
package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.models.cards.Card;
import dk.dtu.roborally.models.cards.CardDeck;
import dk.dtu.roborally.models.player.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author Sebastian
 */
class ProgrammingServiceTest {

    private ProgrammingService service;
    private CardDeck<Card> deck;
    private Card[] cards;

    private static class TestCard extends Card {
    }

    @BeforeEach
    void setUp() {
        service = new ProgrammingService(mock(Player.class));
        deck = new CardDeck<>();

        cards = new Card[9];

        for (int i = 0; i < cards.length; i++) {
            cards[i] = new TestCard();
            deck.addCard(cards[i]);
        }
    }

    @Test
    void shouldNotDrawMoreThanNineCards() {
        deck.addCard(new TestCard());
        deck.addCard(new TestCard());

        service.beginProgrammingPhase(deck);

        assertEquals(9, service.getCards().length);
        assertEquals(2, deck.size());
    }

    @Test
    void shouldNotAllowSameCardInTwoRegisters() {
        service.beginProgrammingPhase(deck);

        service.selectCard(0, cards[0]);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.selectCard(1, cards[0])
        );
    }

    @Test
    void shouldNotSubmitProgramWithEmptyRegisters() {
        service.beginProgrammingPhase(deck);
        service.selectCard(0, cards[0]);

        assertThrows(
                IllegalStateException.class,
                () -> service.submitProgram()
        );
    }

    @Test
    void shouldNotAllowChangesAfterSubmission() {
        service.beginProgrammingPhase(deck);

        Card[] hand = service.getCards();

        for (int i = 0; i < 5; i++) {
            service.selectCard(i, hand[i]);
        }

        service.submitProgram();

        assertThrows(
                IllegalStateException.class,
                () -> service.selectCard(0, hand[5])
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.clearRegister(0)
        );
    }

    @Test
    void shouldRemoveSelectedCardsAndAllowNextRound() {
        // Add five extra cards for the next round.
        for (int i = 0; i < 5; i++) {
            deck.addCard(new TestCard());
        }

        service.beginProgrammingPhase(deck);

        Card[] hand = service.getCards();

        for (int i = 0; i < 5; i++) {
            service.selectCard(i, hand[i]);
        }

        service.submitProgram();
        service.finishProgrammingPhase();

        assertEquals(4, service.getCards().length);

        service.beginProgrammingPhase(deck);

        assertEquals(9, service.getCards().length);
        assertEquals(0, deck.size());
    }

    @Test
    void shouldMakeReplacedCardAvailableAgain() {
        service.beginProgrammingPhase(deck);

        service.selectCard(0, cards[0]);
        service.selectCard(0, cards[1]);

        Card[] available = service.getAvailableCards();

        assertTrue(Arrays.asList(available).contains(cards[0]));
        assertFalse(Arrays.asList(available).contains(cards[1]));
    }
}
