package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.ConveyorBeltType;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.enums.RotationDirection;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.ConveyorBelt;
import dk.dtu.roborally.models.board.elements.EnergySpace;
import dk.dtu.roborally.models.board.elements.Gear;
import dk.dtu.roborally.models.board.elements.PushPanel;
import dk.dtu.roborally.models.board.elements.Wall;
import dk.dtu.roborally.models.game.ActivationLog;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardElementsTest {

	private final Map<Vector2D, Space> spaces = new HashMap<>();
	private final ActivationLog log = new ActivationLog();

	@BeforeEach
	void buildBoard() {
		for (int x = 0; x < 6; x++) {
			for (int y = 0; y < 6; y++) {
				spaces.put(new Vector2D(x, y), new Space(new Vector2D(x, y)));
			}
		}
	}

	@Test
	void gearsTurnTheRobotsOnThem() {
		put(1, 1, new Gear(new Vector2D(1, 1), RotationDirection.CLOCKWISE));
		put(2, 2, new Gear(new Vector2D(2, 2),
				RotationDirection.COUNTERCLOCKWISE));
		Player right = player("right", 1, 1, Direction.NORTH);
		Player left = player("left", 2, 2, Direction.NORTH);
		Player none = player("none", 3, 3, Direction.NORTH);

		BoardElements.activate(game(right, left, none), 0, log);

		assertEquals(Direction.EAST, right.getRobot().getOrientation());
		assertEquals(Direction.WEST, left.getRobot().getOrientation());
		assertEquals(Direction.NORTH, none.getRobot().getOrientation());
		assertEquals(1, log.stepCount());
	}

	@Test
	void greenBeltsMoveOneSpaceAndBlueBeltsTwo() {
		belt(0, 0, 4, Direction.EAST, ConveyorBeltType.BLUE_STRAIGHT);
		belt(0, 2, 4, Direction.EAST, ConveyorBeltType.GREEN_STRAIGHT);
		Player blue = player("blue", 0, 0, Direction.NORTH);
		Player green = player("green", 0, 2, Direction.NORTH);

		BoardElements.activate(game(blue, green), 0, log);

		assertEquals(new Vector2D(2, 0), blue.getRobot().getCurrentSpace());
		assertEquals(new Vector2D(1, 2), green.getRobot().getCurrentSpace());
		assertEquals(Direction.NORTH, blue.getRobot().getOrientation());
	}

	@Test
	void blueBeltStopsCarryingOnceTheRobotIsOffIt() {
		belt(0, 0, 1, Direction.EAST, ConveyorBeltType.BLUE_STRAIGHT);
		Player ada = player("ada", 0, 0, Direction.NORTH);

		BoardElements.activate(game(ada), 0, log);

		assertEquals(new Vector2D(1, 0), ada.getRobot().getCurrentSpace());
	}

	@Test
	void blueOntoGreenGetsThreeSpaces() {
		belt(0, 0, 2, Direction.EAST, ConveyorBeltType.BLUE_STRAIGHT);
		belt(2, 0, 3, Direction.EAST, ConveyorBeltType.GREEN_STRAIGHT);
		Player ada = player("ada", 0, 0, Direction.NORTH);

		BoardElements.activate(game(ada), 0, log);

		assertEquals(new Vector2D(3, 0), ada.getRobot().getCurrentSpace());
	}

	@Test
	void beltsDoNotPushRobotsOffTheBelt() {
		belt(0, 0, 2, Direction.EAST, ConveyorBeltType.GREEN_STRAIGHT);
		Player ada = player("ada", 1, 0, Direction.NORTH);
		Player bob = player("bob", 2, 0, Direction.NORTH);

		BoardElements.activate(game(ada, bob), 0, log);

		assertEquals(new Vector2D(1, 0), ada.getRobot().getCurrentSpace());
		assertEquals(new Vector2D(2, 0), bob.getRobot().getCurrentSpace());
	}

	@Test
	void robotsOnTheSameBeltMoveTogether() {
		belt(0, 0, 4, Direction.EAST, ConveyorBeltType.GREEN_STRAIGHT);
		Player ada = player("ada", 0, 0, Direction.NORTH);
		Player bob = player("bob", 1, 0, Direction.NORTH);

		BoardElements.activate(game(ada, bob), 0, log);

		assertEquals(new Vector2D(1, 0), ada.getRobot().getCurrentSpace());
		assertEquals(new Vector2D(2, 0), bob.getRobot().getCurrentSpace());
	}

	@Test
	void twoRobotsHeadingForTheSameSpaceBothStay() {
		belt(0, 1, 1, Direction.EAST, ConveyorBeltType.GREEN_STRAIGHT);
		belt(1, 0, 1, Direction.SOUTH, ConveyorBeltType.GREEN_STRAIGHT);
		Player ada = player("ada", 0, 1, Direction.NORTH);
		Player bob = player("bob", 1, 0, Direction.NORTH);

		BoardElements.activate(game(ada, bob), 0, log);

		assertEquals(new Vector2D(0, 1), ada.getRobot().getCurrentSpace());
		assertEquals(new Vector2D(1, 0), bob.getRobot().getCurrentSpace());
	}

	@Test
	void aBeltTurningACornerTurnsTheRobot() {
		belt(0, 0, 2, Direction.EAST, ConveyorBeltType.GREEN_STRAIGHT);
		belt(2, 0, 2, Direction.SOUTH, ConveyorBeltType.GREEN_STRAIGHT);
		Player ada = player("ada", 1, 0, Direction.EAST);

		BoardElements.activate(game(ada), 0, log);

		assertEquals(new Vector2D(2, 0), ada.getRobot().getCurrentSpace());
		assertEquals(Direction.SOUTH, ada.getRobot().getOrientation());
	}

	@Test
	void aBeltCanCarryARobotOffTheBoard() {
		belt(5, 0, 1, Direction.EAST, ConveyorBeltType.GREEN_STRAIGHT);
		Player ada = player("ada", 5, 0, Direction.NORTH);

		BoardElements.activate(game(ada), 0, log);

		assertEquals(new Vector2D(5, 5), ada.getRobot().getCurrentSpace());
		assertTrue(ada.getRobot().isRebootedThisRound());
	}

	@Test
	void wallsStopBelts() {
		belt(0, 0, 3, Direction.EAST, ConveyorBeltType.GREEN_STRAIGHT);
		put(0, 0, new Wall(new Vector2D(0, 0), Direction.EAST));
		Player ada = player("ada", 0, 0, Direction.NORTH);

		BoardElements.activate(game(ada), 0, log);

		assertEquals(new Vector2D(0, 0), ada.getRobot().getCurrentSpace());
	}

	@Test
	void pushPanelsOnlyPushInTheirRegisters() {
		put(1, 1, new PushPanel(new Vector2D(1, 1), Direction.EAST,
				Set.of(2, 4)));
		Player ada = player("ada", 1, 1, Direction.NORTH);
		Game game = game(ada);

		BoardElements.activate(game, 0, log);
		assertEquals(new Vector2D(1, 1), ada.getRobot().getCurrentSpace());

		BoardElements.activate(game, 1, log);
		assertEquals(new Vector2D(2, 1), ada.getRobot().getCurrentSpace());
		assertEquals(Direction.NORTH, ada.getRobot().getOrientation());
	}

	@Test
	void aPushPanelPushesTheRobotsInTheWayToo() {
		put(1, 1, new PushPanel(new Vector2D(1, 1), Direction.EAST, Set.of(1)));
		Player ada = player("ada", 1, 1, Direction.NORTH);
		Player bob = player("bob", 2, 1, Direction.NORTH);

		BoardElements.activate(game(ada, bob), 0, log);

		assertEquals(new Vector2D(3, 1), bob.getRobot().getCurrentSpace());
	}

	@Test
	void energySpacesGiveTheirCubeOnce() {
		EnergySpace battery = new EnergySpace(new Vector2D(1, 1), 1);
		put(1, 1, battery);
		Player ada = player("ada", 1, 1, Direction.NORTH);
		Game game = game(ada);
		int energy = ada.getEnergy();

		BoardElements.collectEnergy(game, 0);
		BoardElements.collectEnergy(game, 1);

		assertEquals(energy + 1, ada.getEnergy());
		assertEquals(0, battery.getCubeAmount());
	}

	@Test
	void endingTheFifthRegisterOnAnEmptyEnergySpaceTakesFromTheBank() {
		put(1, 1, new EnergySpace(new Vector2D(1, 1), 0));
		Player ada = player("ada", 1, 1, Direction.NORTH);
		int energy = ada.getEnergy();

		BoardElements.collectEnergy(game(ada), 4);

		assertEquals(energy + 1, ada.getEnergy());
	}

	/** A belt running from x,y in the given direction. */
	private void belt(int x, int y, int length, Direction direction,
			ConveyorBeltType colour) {
		ConveyorBelt belt = new ConveyorBelt(new Vector2D(x, y), length,
				direction, colour);
		Vector2D position = new Vector2D(x, y);
		for (int i = 0; i < length; i++) {
			spaces.get(position).addBoardElement(belt);
			position = position.add(direction.step());
		}
	}

	void put(int x, int y, BoardElement element) {
		spaces.get(new Vector2D(x, y)).addBoardElement(element);
	}

	Game game(Player... players) {
		return new Game("game", new Board(spaces), Set.of(players));
	}

	static Player player(String name, int x, int y, Direction facing) {
		return new Player(name, new User(name), PlayerColor.RED,
				new Robot(new Vector2D(x, y), facing, new Vector2D(5, 5)));
	}
}
