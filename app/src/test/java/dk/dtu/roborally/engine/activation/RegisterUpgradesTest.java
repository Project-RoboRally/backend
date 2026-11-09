package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.PriorityAntenna;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.RoundResult;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RegisterUpgradesTest {

	private final RoundActivator activator = new RoundActivator(
			mock(GameEventPublisher.class));
	private Player near;
	private Player far;
	private Game game;

	@BeforeEach
	void setUp() {
		Map<Vector2D, Space> spaces = new HashMap<>();
		for (int x = 0; x < 6; x++) {
			for (int y = 0; y < 6; y++) {
				spaces.put(new Vector2D(x, y), new Space(new Vector2D(x, y)));
			}
		}
		spaces.get(new Vector2D(0, 0)).addBoardElement(new PriorityAntenna());
		near = player("near", 1, 1, Direction.SOUTH);
		far = player("far", 4, 4, Direction.SOUTH);
		game = new Game("game", new Board(spaces), Set.of(near, far));
		program(near);
		program(far);
	}

	@Test
	void manualSortGoesFirstForTheRegisterAndIsUsedUp() {
		far.getUpgrades().install(UpgradeType.MANUAL_SORT, null);

		activator.activate(game);
		PendingChoice choice = game.getCurrentRound().getPendingChoice()
				.orElseThrow();
		assertEquals(ChoiceType.REGISTER_UPGRADE, choice.type());
		assertEquals(List.of("NONE", "MANUAL_SORT"), choice.options());

		game.getCurrentRound().answer("far", "MANUAL_SORT");
		RoundResult result = activator.activate(game);

		assertFalse(far.getUpgrades().has(UpgradeType.MANUAL_SORT));
		assertEquals(List.of("far", "near"), playedBy(result, 0));
		assertEquals(List.of("near", "far"), playedBy(result, 1));
	}

	@Test
	void adminPrivilegeCanOnlyBeUsedOnceARound() {
		far.getUpgrades().install(UpgradeType.ADMIN_PRIVILEGE, null);

		activator.activate(game);
		game.getCurrentRound().answer("far", "ADMIN_PRIVILEGE");
		RoundResult result = activator.activate(game);

		// Not asked again, the round runs to the end
		assertFalse(game.getCurrentRound().isActivating());
		assertEquals(List.of("far", "near"), playedBy(result, 0));
		assertTrue(
				far.getUpgrades().usedThisRound(UpgradeType.ADMIN_PRIVILEGE));
	}

	@Test
	void deflectorShieldCostsOneEnergyAndStopsRobotLasers() {
		// near faces far, so far gets shot every register
		near.getRobot().face(Direction.EAST);
		far.getRobot().moveTo(new Vector2D(4, 1));
		far.getUpgrades().install(UpgradeType.DEFLECTOR_SHIELD, null);
		int energy = far.getEnergy();

		activator.activate(game);
		game.getCurrentRound().answer("far", "DEFLECTOR_SHIELD");
		activator.activate(game);
		// Asked again every register, only shielded in the first
		while (game.getCurrentRound().getPendingChoice().isPresent()) {
			game.getCurrentRound().answer("far", "NONE");
			activator.activate(game);
		}

		// Paid 1, and the five Power Ups gave 5
		assertEquals(energy - 1 + 5, far.getEnergy());
		assertEquals(4, far.getPlayerMat().getDiscardPile().stream()
				.filter(card -> card.getName().equals("SPAM")).count());
	}

	private static List<String> playedBy(RoundResult result, int register) {
		return result.registers().get(register).cards().stream()
				.map(played -> played.username()).toList();
	}

	private void program(Player player) {
		for (int i = 0; i < 5; i++) {
			player.getPlayerMat().getRegisters().get(i)
					.setCard(new ProgrammingCard(CommandInstruction.POWER_UP));
		}
	}

	private static Player player(String name, int x, int y, Direction facing) {
		return new Player(name, new User(name), PlayerColor.RED,
				new Robot(new Vector2D(x, y), facing));
	}
}
