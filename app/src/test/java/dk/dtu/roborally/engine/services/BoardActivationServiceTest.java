package dk.dtu.roborally.engine.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dk.dtu.roborally.enums.ConveyorBeltType;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.enums.RotationDirection;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.ConveyorBelt;
import dk.dtu.roborally.models.board.elements.EnergySpace;
import dk.dtu.roborally.models.board.elements.Gear;
import dk.dtu.roborally.models.board.elements.LaserEmitter;
import dk.dtu.roborally.models.board.elements.PushPanel;
import dk.dtu.roborally.models.board.elements.Wall;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;

class BoardActivationServiceTest {

	private final BoardActivationService service = new BoardActivationService();

	@Test
	void boardLaserDealsItsConfiguredDamage() {
		Map<Vector2D, Space> spaces = horizontalSpaces(5);
		Vector2D source = new Vector2D(0, 0);
		spaces.get(source)
				.addBoardElement(new LaserEmitter(source, Direction.EAST, 2));
		Player target = player("target", new Vector2D(2, 0), Direction.NORTH);

		service.activateRegister(0, List.of(target), new Board(spaces));

		assertEquals(2, target.getPlayerMat().getDiscardPile().size());
	}

	@Test
	void wallStopsBoardLaser() {
		Map<Vector2D, Space> spaces = horizontalSpaces(5);
		Vector2D source = new Vector2D(0, 0);
		spaces.get(source)
				.addBoardElement(new LaserEmitter(source, Direction.EAST, 2));
		spaces.get(new Vector2D(1, 0))
				.addBoardElement(new Wall(new Vector2D(1, 0), Direction.WEST));
		Player target = player("target", new Vector2D(2, 0), Direction.NORTH);

		service.activateRegister(0, List.of(target), new Board(spaces));

		assertEquals(0, target.getPlayerMat().getDiscardPile().size());
	}

	@Test
	void robotLaserDamagesOnlyTheFirstRobotInItsPath() {
		Map<Vector2D, Space> spaces = horizontalSpaces(5);
		Player shooter = player("shooter", new Vector2D(0, 0), Direction.EAST);
		Player firstTarget = player("first", new Vector2D(2, 0),
				Direction.NORTH);
		Player secondTarget = player("second", new Vector2D(4, 0),
				Direction.NORTH);

		service.activateRegister(0, List.of(shooter, firstTarget, secondTarget),
				new Board(spaces));

		assertEquals(1, firstTarget.getPlayerMat().getDiscardPile().size());
		assertEquals(0, secondTarget.getPlayerMat().getDiscardPile().size());
	}

	@Test
	void expressConveyorMovesTwoSpacesAndRotatesRobot() {
		Map<Vector2D, Space> spaces = horizontalSpaces(4);
		spaces.get(new Vector2D(0, 0))
				.addBoardElement(new ConveyorBelt(new Vector2D(0, 0), 1,
						Direction.EAST, ConveyorBeltType.GREEN_RIGHT_CURVE));
		Player player = player("player", new Vector2D(0, 0), Direction.NORTH);

		service.activateRegister(0, List.of(player), new Board(spaces));

		assertEquals(new Vector2D(2, 0), player.getRobot().getCurrentSpace());
		assertEquals(Direction.EAST, player.getRobot().getOrientation());
	}

	@Test
	void regularConveyorMovesOneSpace() {
		Map<Vector2D, Space> spaces = horizontalSpaces(3);
		spaces.get(new Vector2D(0, 0))
				.addBoardElement(new ConveyorBelt(new Vector2D(0, 0), 1,
						Direction.EAST, ConveyorBeltType.BLUE_STRAIGHT));
		Player player = player("player", new Vector2D(0, 0), Direction.NORTH);

		service.activateRegister(0, List.of(player), new Board(spaces));

		assertEquals(new Vector2D(1, 0), player.getRobot().getCurrentSpace());
	}

	@Test
	void pushPanelPushesRobotChain() {
		Map<Vector2D, Space> spaces = horizontalSpaces(3);
		spaces.get(new Vector2D(0, 0)).addBoardElement(new PushPanel(
				new Vector2D(0, 0), Direction.EAST, Set.of(1, 2, 3, 4, 5)));
		Player panelRobot = player("panel", new Vector2D(0, 0),
				Direction.NORTH);
		Player pushedRobot = player("pushed", new Vector2D(1, 0),
				Direction.NORTH);

		service.activateRegister(0, List.of(panelRobot, pushedRobot),
				new Board(spaces));

		assertEquals(new Vector2D(1, 0),
				panelRobot.getRobot().getCurrentSpace());
		assertEquals(new Vector2D(2, 0),
				pushedRobot.getRobot().getCurrentSpace());
	}

	@Test
	void energySpaceAwardsItsCubeAndFifthRegisterBonus() {
		Map<Vector2D, Space> spaces = horizontalSpaces(1);
		EnergySpace energySpace = new EnergySpace(new Vector2D(0, 0), 1);
		spaces.get(new Vector2D(0, 0)).addBoardElement(energySpace);
		Player player = player("player", new Vector2D(0, 0), Direction.NORTH);

		service.activateRegister(4, List.of(player), new Board(spaces));

		assertEquals(7, player.getEnergy());
		assertEquals(0, energySpace.getCubeAmount());
	}

	@Test
	void gearRotatesRobotDuringRegisterActivation() {
		Map<Vector2D, Space> spaces = horizontalSpaces(1);
		spaces.get(new Vector2D(0, 0)).addBoardElement(
				new Gear(new Vector2D(0, 0), RotationDirection.CLOCKWISE));
		Player player = player("player", new Vector2D(0, 0), Direction.NORTH);

		service.activateRegister(0, List.of(player), new Board(spaces));

		assertEquals(Direction.EAST, player.getRobot().getOrientation());
	}

	private Map<Vector2D, Space> horizontalSpaces(int count) {
		Map<Vector2D, Space> spaces = new HashMap<>();
		for (int x = 0; x < count; x++) {
			Vector2D position = new Vector2D(x, 0);
			spaces.put(position, new Space(position));
		}
		return spaces;
	}

	private Player player(String name, Vector2D position,
			Direction orientation) {
		return new Player(name, new User(name), PlayerColor.RED,
				new Robot(position, orientation));
	}
}
