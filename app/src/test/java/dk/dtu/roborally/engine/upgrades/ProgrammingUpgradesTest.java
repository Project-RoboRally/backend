package dk.dtu.roborally.engine.upgrades;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProgrammingUpgradesTest {

	private Player ada;
	private Game game;

	@BeforeEach
	void setUp() {
		ada = new Player("ada", new User("ada"), PlayerColor.RED,
				new Robot(new Vector2D(0, 0), Direction.NORTH));
		game = new Game("game", new Board(Map.of()), Set.of(ada));
	}

	@Test
	void rechargeGivesThreeEnergyAndIsUsedUp() {
		ada.getUpgrades().install(UpgradeType.RECHARGE, null);
		int energy = ada.getEnergy();

		ProgrammingUpgrades.use(game, ada, UpgradeType.RECHARGE, List.of());

		assertEquals(energy + 3, ada.getEnergy());
		assertFalse(ada.getUpgrades().has(UpgradeType.RECHARGE));
	}

	@Test
	void recompileDealsAWholeNewHand() {
		ada.getUpgrades().install(UpgradeType.RECOMPILE, null);
		mat(List.of("MOVE_1", "MOVE_1", "MOVE_1", "MOVE_1", "MOVE_1", "MOVE_1",
				"MOVE_1", "MOVE_1", "MOVE_1"), "U_TURN", "U_TURN", "U_TURN",
				"U_TURN", "U_TURN", "U_TURN", "U_TURN", "U_TURN", "U_TURN");

		ProgrammingUpgrades.use(game, ada, UpgradeType.RECOMPILE, List.of());

		assertEquals(9, ada.getPlayerMat().getHand().size());
		assertEquals(9, count(ada.getPlayerMat().getHand(), "U_TURN"));
		assertEquals(9, count(ada.getPlayerMat().getDiscardPile(), "MOVE_1"));
	}

	@Test
	void spamBlockerSwapsTheSpamInTheHandForNewCards() {
		ada.getUpgrades().install(UpgradeType.SPAM_BLOCKER, null);
		mat(List.of("SPAM", "SPAM", "MOVE_1"), "LEFT_TURN", "RIGHT_TURN");

		ProgrammingUpgrades.use(game, ada, UpgradeType.SPAM_BLOCKER, List.of());

		assertEquals(List.of("MOVE_1", "RIGHT_TURN", "LEFT_TURN"),
				names(ada.getPlayerMat().getHand()));
		assertEquals(40, game.getDamagePiles().size(DamageInstruction.SPAM));
	}

	@Test
	void onlyInstalledUpgradesCanBeUsed() {
		assertThrows(IllegalArgumentException.class, () -> ProgrammingUpgrades
				.use(game, ada, UpgradeType.RECHARGE, List.of()));
	}

	@Test
	void aMemoryStickMeansTenCards() {
		assertEquals(9, ProgrammingUpgrades.handSize(ada));
		ada.getUpgrades().install(UpgradeType.MEMORY_STICK, null);
		assertEquals(10, ProgrammingUpgrades.handSize(ada));
	}

	/** Sets the hand, and the deck with its top card last. */
	private void mat(List<String> hand, String... deck) {
		List<ProgrammableCard> registers = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			registers.add(null);
		}
		ada.getPlayerMat().restore(cards(Arrays.asList(deck)), cards(hand),
				List.of(), registers, false);
	}

	private static List<ProgrammableCard> cards(List<String> names) {
		return names.stream().map(ProgrammableCard::named).toList();
	}

	private static List<String> names(List<ProgrammableCard> cards) {
		return cards.stream().map(ProgrammableCard::getName).toList();
	}

	private static long count(List<ProgrammableCard> cards, String name) {
		return cards.stream().filter(card -> card.getName().equals(name))
				.count();
	}
}
