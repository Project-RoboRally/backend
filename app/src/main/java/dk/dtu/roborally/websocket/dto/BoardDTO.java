package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.RobotView;

import java.util.Comparator;
import java.util.List;

/**
 * The board layout. Width and height are derived from the furthest space.
 *
 * @author Elias, Nicoleta
 */
public record BoardDTO(int width, int height, List<SpaceDTO> spaces,
		List<RobotDTO> robots, List<List<RobotDTO>> steps) {

	public record SpaceDTO(int x, int y, List<BoardElementDTO> elements) {

		public static SpaceDTO from(Space space) {
			return new SpaceDTO(space.getPosition().x(),
					space.getPosition().y(), space.getBoardElements().stream()
							.map(BoardElementDTO::from).toList());
		}
	}

	/**
	 * Where a player's robot is standing, and which way it faces.
	 */
	public record RobotDTO(String username, PlayerColor color, int x, int y,
			Direction orientation) {

		public static RobotDTO from(Player player) {
			return from(RobotView.from(player));
		}

		public static RobotDTO from(RobotView view) {
			return new RobotDTO(view.username(), view.color(), view.x(),
					view.y(), view.orientation());
		}
	}

	public static BoardDTO from(Board board) {
		return from(board, List.of());
	}

	public static BoardDTO from(Game game) {
		return from(game, List.of());
	}

	public static BoardDTO from(Game game, List<List<RobotView>> steps) {
		List<RobotDTO> robots = robotsOf(game);
		List<List<RobotDTO>> frames = steps.stream()
				.map(frame -> frame.stream().map(RobotDTO::from)
						.sorted(Comparator.comparing(RobotDTO::username))
						.toList())
				.toList();
		return from(game.getBoard(), robots, frames);
	}

	private static List<RobotDTO> robotsOf(Game game) {
		return game.getPlayersInGame().stream().map(RobotDTO::from)
				.sorted(Comparator.comparing(RobotDTO::username)).toList();
	}

	private static BoardDTO from(Board board, List<RobotDTO> robots) {
		return from(board, robots, List.of());
	}

	private static BoardDTO from(Board board, List<RobotDTO> robots,
			List<List<RobotDTO>> steps) {
		// Sort spaces by y coordinate, then x coordinate
		List<SpaceDTO> spaces = board.getSpaceMap().values().stream()
				.map(SpaceDTO::from).sorted(Comparator.comparingInt(SpaceDTO::y)
						.thenComparingInt(SpaceDTO::x))
				.toList();

		// Get the width and height of the board or set to 0 if no spaces are
		// found
		int width = spaces.stream().mapToInt(SpaceDTO::x).max().orElse(-1) + 1;
		int height = spaces.stream().mapToInt(SpaceDTO::y).max().orElse(-1) + 1;
		return new BoardDTO(width, height, spaces, robots, steps);
	}
}
