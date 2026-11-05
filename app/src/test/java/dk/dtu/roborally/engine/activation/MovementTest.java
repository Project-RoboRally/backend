package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.PriorityAntenna;
import dk.dtu.roborally.models.board.elements.Wall;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovementTest {

	private final Map<Vector2D, Space> spaces = new HashMap<>();

	@Test
	void pushesARobotInTheWayWithoutTurningIt() {
		row(4);
		Player ada = player("ada", 0, Direction.EAST);
		Player bob = player("bob", 1, Direction.NORTH);
		Game game = game(ada, bob);

		assertTrue(Movement.step(game, ada, Direction.EAST));

		assertEquals(new Vector2D(1, 0), ada.getRobot().getCurrentSpace());
		assertEquals(new Vector2D(2, 0), bob.getRobot().getCurrentSpace());
		assertEquals(Direction.NORTH, bob.getRobot().getOrientation());
	}

	@Test
	void pushesAWholeLineOfRobots() {
		row(4);
		Player ada = player("ada", 0, Direction.EAST);
		Player bob = player("bob", 1, Direction.EAST);
		Player cat = player("cat", 2, Direction.EAST);
		Game game = game(ada, bob, cat);

		assertTrue(Movement.step(game, ada, Direction.EAST));

		assertEquals(new Vector2D(3, 0), cat.getRobot().getCurrentSpace());
	}

	@Test
	void aWallBehindThePushedRobotStopsEveryone() {
		row(3);
		spaces.get(new Vector2D(1, 0))
				.addBoardElement(new Wall(new Vector2D(1, 0), Direction.EAST));
		Player ada = player("ada", 0, Direction.EAST);
		Player bob = player("bob", 1, Direction.EAST);
		Game game = game(ada, bob);

		assertFalse(Movement.step(game, ada, Direction.EAST));

		assertEquals(new Vector2D(0, 0), ada.getRobot().getCurrentSpace());
		assertEquals(new Vector2D(1, 0), bob.getRobot().getCurrentSpace());
	}

	@Test
	void nothingCanMoveOrBePushedOntoTheAntenna() {
		row(3);
		spaces.get(new Vector2D(2, 0)).addBoardElement(new PriorityAntenna());
		Player ada = player("ada", 0, Direction.EAST);
		Player bob = player("bob", 1, Direction.EAST);
		Game game = game(ada, bob);

		assertFalse(Movement.step(game, bob, Direction.EAST));
		assertFalse(Movement.step(game, ada, Direction.EAST));

		assertEquals(new Vector2D(1, 0), bob.getRobot().getCurrentSpace());
	}

	private void row(int length) {
		for (int x = 0; x < length; x++) {
			spaces.put(new Vector2D(x, 0), new Space(new Vector2D(x, 0)));
		}
	}

	private Game game(Player... players) {
		return new Game("game", new Board(spaces), Set.of(players));
	}

	private static Player player(String name, int x, Direction facing) {
		return new Player(name, new User(name), PlayerColor.RED,
				new Robot(new Vector2D(x, 0), facing));
	}
}
