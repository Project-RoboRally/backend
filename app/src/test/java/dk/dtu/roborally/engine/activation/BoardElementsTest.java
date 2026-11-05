package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.enums.RotationDirection;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.Gear;
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
