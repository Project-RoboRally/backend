package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.RebootPoint;
import dk.dtu.roborally.models.cards.DamageCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RebootTest {

	private final Map<Vector2D, Space> spaces = new HashMap<>();

	@Test
	void rebootingTakesTwoSpamAndGoesToTheTokenCoveringTheSpace() {
		Player ada = player("ada", 1, 1, new Vector2D(0, 4));

		Reboot.reboot(game(ada), ada, new Vector2D(2, 2));

		assertEquals(new Vector2D(3, 0), ada.getRobot().getCurrentSpace());
		assertTrue(ada.getRobot().isRebootedThisRound());
		assertEquals(List.of("SPAM", "SPAM"),
				ada.getPlayerMat().getDiscardPile().stream()
						.map(card -> card.getName()).toList());
	}

	@Test
	void fallingFromTheStartBoardGoesBackToTheStartSpace() {
		Player ada = player("ada", 0, 4, new Vector2D(0, 4));

		Reboot.reboot(game(ada), ada, new Vector2D(0, 4));

		assertEquals(new Vector2D(0, 4), ada.getRobot().getCurrentSpace());
	}

	@Test
	void aRobotOnTheTokenIsPushedTheWayTheTokenPoints() {
		Player ada = player("ada", 1, 1, new Vector2D(0, 4));
		Player bob = player("bob", 3, 0, new Vector2D(1, 4));
		Game both = game(ada, bob);

		Reboot.reboot(both, ada, new Vector2D(1, 1));

		assertEquals(new Vector2D(3, 0), ada.getRobot().getCurrentSpace());
		assertEquals(new Vector2D(3, 1), bob.getRobot().getCurrentSpace());
	}

	@Test
	void theRestOfTheProgramIsCancelled() {
		Game game = game(player("ada", 1, 1, new Vector2D(0, 4)));
		Player ada = game.getPlayersInGame().iterator().next();
		for (int i = 0; i < 5; i++) {
			ada.getPlayerMat().getRegisters().get(i)
					.setCard(new ProgrammingCard(CommandInstruction.MOVE_1));
		}
		game.getCurrentRound().startRegister(2, List.of(ada.getId()));

		Reboot.reboot(game, ada, new Vector2D(1, 1));

		assertEquals("MOVE_1",
				ada.getPlayerMat().getRegisters().get(1).getCard().getName());
		assertNull(ada.getPlayerMat().getRegisters().get(2).getCard());
		assertNull(ada.getPlayerMat().getRegisters().get(4).getCard());
	}

	@Test
	void aWormRebootsAndThePlayerPicksTheFacing() {
		Game game = game(player("ada", 1, 1, new Vector2D(0, 4)));
		Player ada = game.getPlayersInGame().iterator().next();
		ada.getPlayerMat().getRegisters().get(0)
				.setCard(new DamageCard(DamageInstruction.WORM));
		for (int i = 1; i < 5; i++) {
			ada.getPlayerMat().getRegisters().get(i)
					.setCard(new ProgrammingCard(CommandInstruction.MOVE_1));
		}
		RoundActivator activator = new RoundActivator(
				mock(GameEventPublisher.class));

		activator.activate(game);

		assertEquals(ChoiceType.REBOOT_FACING,
				game.getCurrentRound().getPendingChoice().orElseThrow().type());
		assertEquals(new Vector2D(3, 0), ada.getRobot().getCurrentSpace());

		game.getCurrentRound().answer("ada", "WEST");
		activator.activate(game);

		// Facing west, but the Move 1 cards were cancelled
		assertEquals(Direction.WEST, ada.getRobot().getOrientation());
		assertEquals(new Vector2D(3, 0), ada.getRobot().getCurrentSpace());
		assertTrue(game.getCurrentRound().getPendingChoice().isEmpty());
	}

	/**
	 * A 4x4 main board with a reboot token at 3,0 pointing south, and a start
	 * row below it at y 4 that the token does not cover.
	 */
	@BeforeEach
	void buildBoard() {
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 5; y++) {
				spaces.put(new Vector2D(x, y), new Space(new Vector2D(x, y)));
			}
		}
		spaces.get(new Vector2D(3, 0)).addBoardElement(
				new RebootPoint(new Vector2D(3, 0), Direction.SOUTH,
						new Vector2D(0, 0), new Vector2D(3, 3)));
	}

	private Game game(Player... players) {
		return new Game("game", new Board(spaces), Set.of(players));
	}

	private static Player player(String name, int x, int y, Vector2D start) {
		return new Player(name, new User(name), PlayerColor.RED,
				new Robot(new Vector2D(x, y), Direction.NORTH, start));
	}
}
