package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.cards.UpgradeCard;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpgradeShopTest {

	@Test
	void refillDealsOneCardPerPlayer() {
		UpgradeShop shop = new UpgradeShop();

		shop.refill(4);

		assertEquals(4, shop.getCards().size());
		// One of every upgrade, minus the four for sale
		assertEquals(UpgradeType.values().length - 4, shop.getDeckSize());
	}

	@Test
	void onlyBoughtCardsAreReplaced() {
		UpgradeShop shop = new UpgradeShop();
		shop.refill(3);
		UpgradeCard bought = shop.getCards().get(0);
		List<UpgradeCard> kept = new ArrayList<>(shop.getCards());
		kept.remove(bought);

		assertSame(bought, shop.buy(bought.getType()));
		assertEquals(2, shop.getCards().size());
		shop.refill(3);

		assertEquals(3, shop.getCards().size());
		assertTrue(shop.getCards().containsAll(kept));
	}

	@Test
	void fullShopIsReplacedCompletely() {
		UpgradeShop shop = new UpgradeShop();
		shop.refill(3);
		List<UpgradeCard> before = new ArrayList<>(shop.getCards());

		shop.refill(3);

		assertEquals(3, shop.getCards().size());
		for (UpgradeCard card : before) {
			assertFalse(shop.getCards().contains(card));
		}
	}

	@Test
	void theShopShrinksOnceTheDeckRunsOut() {
		UpgradeShop shop = new UpgradeShop();

		for (int round = 0; round < 10; round++) {
			shop.refill(6);
		}

		// 40 cards go 6 at a time, the last refill only had 4 left
		assertEquals(4, shop.getCards().size());
		assertEquals(0, shop.getDeckSize());
	}

	@Test
	void cardThatIsNotInTheShopCannotBeBought() {
		UpgradeShop shop = new UpgradeShop();

		assertThrows(IllegalArgumentException.class,
				() -> shop.buy(UpgradeType.SPEED_ROUTINE));
	}
}
