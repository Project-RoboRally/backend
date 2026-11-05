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
import dk.dtu.roborally.models.board.elements.Wall;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class TurnUpgradesTest {

	private final RoundActivator activator = new RoundActivator(
			mock(GameEventPublisher.class));
	private Player ada;
	private Game game;

	@BeforeEach
	void setUp() {
		Map<Vector2D, Space> spaces = new HashMap<>();
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 5; y++) {
				spaces.put(new Vector2D(x, y), new Space(new Vector2D(x, y)));
			}
		}
		ada = new Player("ada", new User("ada"), PlayerColor.RED,
				new Robot(new Vector2D(2, 2), Direction.NORTH));
		game = new Game("game", new Board(spaces), Set.of(ada));
	}

	@Test
	void zoopTurnsTheRobotBeforeItsCard() {
		ada.getUpgrades().install(UpgradeType.ZOOP, null);
		program(CommandInstruction.MOVE_1, CommandInstruction.POWER_UP);

		answer(ChoiceType.TURN_UPGRADE, "ZOOP");
		answer(ChoiceType.ZOOP_FACING, "EAST");

		assertEquals(Direction.EAST, ada.getRobot().getOrientation());
		assertEquals(new Vector2D(3, 2), ada.getRobot().getCurrentSpace());
		assertFalse(ada.getUpgrades().has(UpgradeType.ZOOP));
	}

	@Test
	void boinkMovesToAnAdjacentSpaceWithoutTurning() {
		ada.getUpgrades().install(UpgradeType.BOINK, null);
		program(CommandInstruction.POWER_UP, CommandInstruction.POWER_UP);

		answer(ChoiceType.TURN_UPGRADE, "BOINK");
		answer(ChoiceType.BOINK_DIRECTION, "WEST");

		assertEquals(new Vector2D(1, 2), ada.getRobot().getCurrentSpace());
		assertEquals(Direction.NORTH, ada.getRobot().getOrientation());
	}

	@Test
	void rechargeAndThenNothingElse() {
		ada.getUpgrades().install(UpgradeType.RECHARGE, null);
		program(CommandInstruction.POWER_UP, CommandInstruction.POWER_UP);
		int energy = ada.getEnergy();

		answer(ChoiceType.TURN_UPGRADE, "RECHARGE");

		// 3 from Recharge and one per Power Up
		assertEquals(energy + 3 + 5, ada.getEnergy());
		assertFalse(game.getCurrentRound().isActivating());
	}

	@Test
	void sayingNoneKeepsTheUpgradeForLater() {
		ada.getUpgrades().install(UpgradeType.ZOOP, null);
		program(CommandInstruction.POWER_UP, CommandInstruction.POWER_UP);

		activator.activate(game);
		assertEquals(List.of("NONE", "ZOOP"), game.getCurrentRound()
				.getPendingChoice().orElseThrow().options());
		game.getCurrentRound().answer("ada", "NONE");
		activator.activate(game);

		// Asked again on the next turn
		assertEquals(ChoiceType.TURN_UPGRADE,
				game.getCurrentRound().getPendingChoice().orElseThrow().type());
	}

	@Test
	void hackPlaysTheCardTwice() {
		ada.getUpgrades().install(UpgradeType.HACK, null);
		program(CommandInstruction.MOVE_1, CommandInstruction.POWER_UP);

		answer(ChoiceType.TURN_UPGRADE, "HACK");

		assertEquals(new Vector2D(2, 0), ada.getRobot().getCurrentSpace());
	}

	@Test
	void rebootUpgradeRebootsWithoutDamage() {
		ada.getUpgrades().install(UpgradeType.REBOOT, null);
		ada.getRobot().moveTo(new Vector2D(4, 4));
		program(CommandInstruction.MOVE_1, CommandInstruction.MOVE_1);

		answer(ChoiceType.TURN_UPGRADE, "REBOOT");
		answer(ChoiceType.REBOOT_FACING, "SOUTH");

		// Back on its start space, no SPAM, and the program was cancelled
		assertEquals(new Vector2D(2, 2), ada.getRobot().getCurrentSpace());
		assertEquals(Direction.SOUTH, ada.getRobot().getOrientation());
		assertEquals(0, ada.getPlayerMat().getDiscardPile().stream()
				.filter(card -> card.getName().equals("SPAM")).count());
	}

	@Test
	void refreshPlaysAnotherCardAndThrowsOutADamageCard() {
		ada.getUpgrades().install(UpgradeType.REFRESH, null);
		program(CommandInstruction.POWER_UP, CommandInstruction.POWER_UP);
		ada.getPlayerMat().getRegisters().get(0)
				.setCard(new DamageCard(DamageInstruction.SPAM));
		int spam = game.getDamagePiles().size(DamageInstruction.SPAM);

		answer(ChoiceType.TURN_UPGRADE, "REFRESH");
		answer(ChoiceType.REFRESH_CARD, "MOVE_2");

		assertEquals(new Vector2D(2, 0), ada.getRobot().getCurrentSpace());
		assertEquals(spam + 1,
				game.getDamagePiles().size(DamageInstruction.SPAM));
	}

	@Test
	void brakesTurnAMoveOneIntoAMoveZero() {
		ada.getUpgrades().install(UpgradeType.BRAKES, null);
		program(CommandInstruction.MOVE_1, CommandInstruction.POWER_UP);

		activator.activate(game);
		assertEquals(List.of("MOVE_1", "MOVE_0"), game.getCurrentRound()
				.getPendingChoice().orElseThrow().options());
		game.getCurrentRound().answer("ada", "MOVE_0");
		activator.activate(game);

		assertEquals(new Vector2D(2, 2), ada.getRobot().getCurrentSpace());
	}

	@Test
	void crabLegsMoveSidewaysWithoutTurning() {
		ada.getUpgrades().install(UpgradeType.CRAB_LEGS, null);
		program(CommandInstruction.MOVE_1, CommandInstruction.POWER_UP);

		answer(ChoiceType.MOVE_ONE, "LEFT");

		assertEquals(new Vector2D(1, 2), ada.getRobot().getCurrentSpace());
		assertEquals(Direction.NORTH, ada.getRobot().getOrientation());
	}

	@Test
	void aHackedMoveOneOnlyAsksOnce() {
		ada.getUpgrades().install(UpgradeType.CRAB_LEGS, null);
		ada.getUpgrades().install(UpgradeType.HACK, null);
		program(CommandInstruction.MOVE_1, CommandInstruction.POWER_UP);

		answer(ChoiceType.TURN_UPGRADE, "HACK");
		answer(ChoiceType.MOVE_ONE, "RIGHT");

		assertEquals(new Vector2D(4, 2), ada.getRobot().getCurrentSpace());
		assertFalse(game.getCurrentRound().isActivating());
	}

	@Test
	void theTeleporterJumpsOverWallsAndSwapsWithARobot() {
		ada.getUpgrades().install(UpgradeType.TELEPORTER, null);
		game.getBoard().getSpace(2, 1)
				.addBoardElement(new Wall(new Vector2D(2, 1), Direction.NORTH));
		Player bob = new Player("bob", new User("bob"), PlayerColor.BLUE,
				new Robot(new Vector2D(2, 0), Direction.EAST));
		Game both = new Game("game", game.getBoard(), Set.of(ada, bob));
		program(CommandInstruction.MOVE_2, CommandInstruction.POWER_UP);
		int energy = ada.getEnergy();

		activator.activate(both);
		both.getCurrentRound().answer("ada", "YES");
		activator.activate(both);

		assertEquals(new Vector2D(2, 0), ada.getRobot().getCurrentSpace());
		assertEquals(new Vector2D(2, 2), bob.getRobot().getCurrentSpace());
		// Paid 1, then four Power Ups
		assertEquals(energy - 1 + 4, ada.getEnergy());
	}

	@Test
	void sideArmsPushToTheSideInstead() {
		ada.getUpgrades().install(UpgradeType.SIDE_ARMS, null);
		Player bob = new Player("bob", new User("bob"), PlayerColor.BLUE,
				new Robot(new Vector2D(2, 1), Direction.WEST));
		game = new Game("game", game.getBoard(), Set.of(ada, bob));
		program(CommandInstruction.MOVE_1, CommandInstruction.POWER_UP);

		answer(ChoiceType.SIDE_ARMS, "RIGHT");

		assertEquals(new Vector2D(2, 1), ada.getRobot().getCurrentSpace());
		assertEquals(new Vector2D(3, 1), bob.getRobot().getCurrentSpace());
	}

	/** Runs activation to the given choice and answers it. */
	private void answer(ChoiceType type, String answer) {
		activator.activate(game);
		assertEquals(type,
				game.getCurrentRound().getPendingChoice().orElseThrow().type());
		game.getCurrentRound().answer("ada", answer);
		activator.activate(game);
	}

	private void program(CommandInstruction first, CommandInstruction rest) {
		ada.getPlayerMat().getRegisters().get(0)
				.setCard(new ProgrammingCard(first));
		for (int i = 1; i < 5; i++) {
			ada.getPlayerMat().getRegisters().get(i)
					.setCard(new ProgrammingCard(rest));
		}
	}
}
