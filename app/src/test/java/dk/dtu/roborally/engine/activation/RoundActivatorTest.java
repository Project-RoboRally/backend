package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.Pit;
import dk.dtu.roborally.models.cards.DamageCard;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.RoundResult;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RoundActivatorTest {

	private final RoundActivator activator = new RoundActivator(
			mock(GameEventPublisher.class));

	@Test
	void weaselRoutineWaitsForThePlayerToPickATurn() {
		Player ada = player("ada", 1, 1, Direction.NORTH);
		Game game = game(ada);
		program(ada, CommandInstruction.WEASEL_ROUTINE,
				CommandInstruction.POWER_UP);

		activator.activate(game);

		PendingChoice choice = game.getCurrentRound().getPendingChoice()
				.orElseThrow();
		assertEquals(ChoiceType.WEASEL_ACTION, choice.type());
		assertEquals(List.of("LEFT_TURN", "RIGHT_TURN", "U_TURN"),
				choice.options());
		assertTrue(game.getCurrentRound().isActivating());
		assertEquals(Direction.NORTH, ada.getRobot().getOrientation());

		game.getCurrentRound().answer("ada", "RIGHT_TURN");
		activator.activate(game);

		assertFalse(game.getCurrentRound().isActivating());
		assertEquals(Direction.EAST, ada.getRobot().getOrientation());
	}

	@Test
	void sandboxRoutineCanMoveAndTheRestOfTheRoundCarriesOn() {
		Player ada = player("ada", 1, 2, Direction.NORTH);
		Game game = game(ada);
		program(ada, CommandInstruction.SANDBOX_ROUTINE,
				CommandInstruction.POWER_UP);

		activator.activate(game);
		game.getCurrentRound().answer("ada", "MOVE_1");
		activator.activate(game);

		assertEquals(new Vector2D(1, 1), ada.getRobot().getCurrentSpace());
		assertEquals(9, ada.getEnergy());
		assertTrue(game.getCurrentRound().getPendingChoice().isEmpty());
	}

	@Test
	void stepsBeforeTheChoiceAreSentAndNotRepeated() {
		Player ada = player("ada", 0, 1, Direction.EAST);
		Game game = game(ada);
		ada.getPlayerMat().getRegisters().get(0)
				.setCard(new ProgrammingCard(CommandInstruction.MOVE_1));
		for (int i = 1; i < 5; i++) {
			ada.getPlayerMat().getRegisters().get(i).setCard(
					new ProgrammingCard(CommandInstruction.WEASEL_ROUTINE));
		}

		assertEquals(1, activator.activate(game).steps().size());
		game.getCurrentRound().answer("ada", "U_TURN");
		assertEquals(1, activator.activate(game).steps().size());
	}

	@Test
	void againAfterASandboxRoutineAsksAgain() {
		Player ada = player("ada", 1, 1, Direction.NORTH);
		Game game = game(ada);
		program(ada, CommandInstruction.SANDBOX_ROUTINE,
				CommandInstruction.AGAIN);

		activator.activate(game);
		game.getCurrentRound().answer("ada", "LEFT_TURN");
		activator.activate(game);

		assertEquals(ChoiceType.SANDBOX_ACTION,
				game.getCurrentRound().getPendingChoice().orElseThrow().type());
		assertEquals(Direction.WEST, ada.getRobot().getOrientation());
	}

	@Test
	void spamGoesBackToItsPileAndTheTopCardIsPlayedInstead() {
		Player ada = player("ada", 1, 2, Direction.NORTH);
		Game game = game(ada);
		deck(ada, CommandInstruction.MOVE_1);
		ada.getPlayerMat().getRegisters().get(0)
				.setCard(new DamageCard(DamageInstruction.SPAM));
		int spam = game.getDamagePiles().size(DamageInstruction.SPAM);

		RoundResult result = activator.activate(game);

		assertEquals(new Vector2D(1, 1), ada.getRobot().getCurrentSpace());
		assertEquals(spam + 1,
				game.getDamagePiles().size(DamageInstruction.SPAM));
		assertEquals(List.of("SPAM", "MOVE_1"),
				result.registers().get(0).cards().stream()
						.map(played -> played.card().getName()).toList());
	}

	@Test
	void trojanHorseHandsOutTwoSpam() {
		Player ada = player("ada", 1, 1, Direction.NORTH);
		Game game = game(ada);
		deck(ada, CommandInstruction.POWER_UP);
		ada.getPlayerMat().getRegisters().get(0)
				.setCard(new DamageCard(DamageInstruction.TROJAN_HORSE));

		activator.activate(game);

		assertEquals(2, ada.getPlayerMat().getDiscardPile().stream()
				.filter(card -> card.getName().equals("SPAM")).count());
	}

	@Test
	void virusSpreadsToRobotsWithinSixSpaces() {
		Player ada = player("ada", 0, 0, Direction.NORTH);
		Player near = player("near", 2, 2, Direction.NORTH);
		Player far = player("far", 6, 1, Direction.NORTH);
		Game game = game(ada, near, far);
		deck(ada, CommandInstruction.POWER_UP);
		ada.getPlayerMat().getRegisters().get(0)
				.setCard(new DamageCard(DamageInstruction.VIRUS));

		activator.activate(game);

		assertEquals(List.of("VIRUS"), near.getPlayerMat().getDiscardPile()
				.stream().map(card -> card.getName()).toList());
		assertTrue(far.getPlayerMat().getDiscardPile().isEmpty());
		assertTrue(ada.getPlayerMat().getDiscardPile().stream()
				.noneMatch(card -> card.getName().equals("VIRUS")));
	}

	@Test
	void againRightAfterADamageCardPlaysTheTopCardInstead() {
		Player ada = player("ada", 1, 2, Direction.NORTH);
		Game game = game(ada);
		deck(ada, CommandInstruction.LEFT_TURN, CommandInstruction.MOVE_1);
		ada.getPlayerMat().getRegisters().get(0)
				.setCard(new DamageCard(DamageInstruction.SPAM));
		ada.getPlayerMat().getRegisters().get(1)
				.setCard(new ProgrammingCard(CommandInstruction.AGAIN));

		activator.activate(game);

		// Spam became Move 1, then Again became Left Turn instead of a move
		assertEquals(new Vector2D(1, 1), ada.getRobot().getCurrentSpace());
		assertEquals(Direction.WEST, ada.getRobot().getOrientation());
	}

	@Test
	void spamFolderPutsOneSpamFromTheDiscardPileBack() {
		Player ada = player("ada", 1, 1, Direction.NORTH);
		Game game = game(ada);
		Damage.take(game, ada, DamageInstruction.SPAM, 2);
		program(ada, CommandInstruction.SPAM_FOLDER,
				CommandInstruction.POWER_UP);

		activator.activate(game);

		assertEquals(1, ada.getPlayerMat().getDiscardPile().size());
		assertEquals(37, game.getDamagePiles().size(DamageInstruction.SPAM));
	}

	@Test
	void aHoverUnitPassesOverPitsButFallsInWhenItStopsOnOne() {
		Player ada = player("ada", 0, 2, Direction.NORTH);
		Game game = game(ada);
		game.getBoard().getSpace(0, 1)
				.addBoardElement(new Pit(new Vector2D(0, 1)));
		ada.getUpgrades().install(UpgradeType.HOVER_UNIT, null);
		program(ada, CommandInstruction.MOVE_2, CommandInstruction.POWER_UP);

		activator.activate(game);

		assertEquals(new Vector2D(0, 0), ada.getRobot().getCurrentSpace());
		assertFalse(ada.getRobot().isRebootedThisRound());

		Player bob = player("bob", 2, 2, Direction.NORTH);
		Game other = game(bob);
		other.getBoard().getSpace(2, 1)
				.addBoardElement(new Pit(new Vector2D(2, 1)));
		bob.getUpgrades().install(UpgradeType.HOVER_UNIT, null);
		program(bob, CommandInstruction.MOVE_1, CommandInstruction.POWER_UP);

		activator.activate(other);

		assertTrue(bob.getRobot().isRebootedThisRound());
	}

	/** Sets the draw pile, the last card is on top. */
	private static void deck(Player player, CommandInstruction... cards) {
		List<ProgrammableCard> draw = new ArrayList<>();
		for (CommandInstruction card : cards) {
			draw.add(new ProgrammingCard(card));
		}
		List<ProgrammableCard> registers = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			registers.add(null);
		}
		player.getPlayerMat().restore(draw, List.of(), List.of(), registers,
				false);
	}

	/** First register gets the given card, the rest get the filler. */
	private static void program(Player player, CommandInstruction first,
			CommandInstruction rest) {
		player.getPlayerMat().getRegisters().get(0)
				.setCard(new ProgrammingCard(first));
		for (int i = 1; i < 5; i++) {
			player.getPlayerMat().getRegisters().get(i)
					.setCard(new ProgrammingCard(rest));
		}
	}

	private static Game game(Player... players) {
		Map<Vector2D, Space> spaces = new HashMap<>();
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				spaces.put(new Vector2D(x, y), new Space(new Vector2D(x, y)));
			}
		}
		return new Game("game", new Board(spaces), Set.of(players));
	}

	private static Player player(String username, int x, int y,
			Direction facing) {
		return new Player(username, new User(username), PlayerColor.RED,
				new Robot(new Vector2D(x, y), facing));
	}
}
