package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.models.cards.DamageCard;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DamagePilesTest {

	@Test
	void startsWithTheCardsFromTheBox() {
		DamagePiles piles = new DamagePiles();

		assertEquals(38, piles.size(DamageInstruction.SPAM));
		assertEquals(12, piles.size(DamageInstruction.TROJAN_HORSE));
		assertEquals(6, piles.size(DamageInstruction.WORM));
		assertEquals(18, piles.size(DamageInstruction.VIRUS));
	}

	@Test
	void takingAndGivingBackMovesCardsBetweenPileAndPlayer() {
		DamagePiles piles = new DamagePiles();

		DamageCard card = piles.take(DamageInstruction.VIRUS).orElseThrow();

		assertEquals(DamageInstruction.VIRUS, card.getInstruction());
		assertEquals(17, piles.size(DamageInstruction.VIRUS));
		piles.giveBack(card);
		assertEquals(18, piles.size(DamageInstruction.VIRUS));
	}

	@Test
	void anEmptyPileTakesFromTheBiggestOtherPile() {
		DamagePiles piles = new DamagePiles();
		piles.restore(Map.of(DamageInstruction.SPAM, 0,
				DamageInstruction.TROJAN_HORSE, 2, DamageInstruction.WORM, 5,
				DamageInstruction.VIRUS, 1));

		assertEquals(DamageInstruction.WORM, piles.take(DamageInstruction.SPAM)
				.orElseThrow().getInstruction());
	}

	@Test
	void nothingIsTakenWhenEveryPileIsEmpty() {
		DamagePiles piles = new DamagePiles();
		piles.restore(Map.of(DamageInstruction.SPAM, 0,
				DamageInstruction.TROJAN_HORSE, 0, DamageInstruction.WORM, 0,
				DamageInstruction.VIRUS, 0));

		assertEquals(Optional.empty(), piles.take(DamageInstruction.SPAM));
	}
}
