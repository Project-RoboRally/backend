package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.PriorityAntenna;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriorityTest {

	@Test
	void closerRobotsGoFirst() {
		Player far = player("aaa", 5, 2);
		Player near = player("zzz", 3, 2);

		assertEquals(List.of(near, far),
				Priority.order(game(Direction.NORTH, far, near)));
	}

	@Test
	void tiesSweepClockwiseFromWhereTheDishPoints() {
		// All four are 2 spaces from the antenna at 2,2
		Player north = player("north", 2, 0);
		Player east = player("east", 4, 2);
		Player south = player("south", 2, 4);
		Player west = player("west", 0, 2);

		assertEquals(List.of(north, east, south, west), Priority
				.order(game(Direction.NORTH, west, south, east, north)));
		assertEquals(List.of(south, west, north, east), Priority
				.order(game(Direction.SOUTH, west, south, east, north)));
	}

	@Test
	void aRobotJustPastTheLineWaitsForTheWholeSweep() {
		Player rightOfLine = player("right", 3, 1);
		Player leftOfLine = player("left", 1, 1);

		assertEquals(List.of(rightOfLine, leftOfLine),
				Priority.order(game(Direction.NORTH, leftOfLine, rightOfLine)));
	}

	private static Game game(Direction dish, Player... players) {
		Map<Vector2D, Space> spaces = new HashMap<>();
		for (int x = 0; x < 6; x++) {
			for (int y = 0; y < 6; y++) {
				spaces.put(new Vector2D(x, y), new Space(new Vector2D(x, y)));
			}
		}
		spaces.get(new Vector2D(2, 2))
				.addBoardElement(new PriorityAntenna(dish));
		return new Game("game", new Board(spaces), Set.of(players));
	}

	private static Player player(String name, int x, int y) {
		return new Player(name, new User(name), PlayerColor.RED,
				new Robot(new Vector2D(x, y), Direction.NORTH));
	}
}
