package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.RoundResult;
import dk.dtu.roborally.models.game.RoundResult.RegisterReveal;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.RobotView;

import java.util.Comparator;
import java.util.List;

/**
 * The board layout. Width and height are derived from the furthest space.
 *
 * @author Elias, Nicoleta, Sebastian
 */
public record BoardDTO(int width, int height, List<SpaceDTO> spaces,
		List<RobotDTO> robots, List<List<RobotDTO>> steps,
		List<RegisterDTO> registers) {

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

	/**
	 * The cards revealed in a register. firstStep and stepCount point into
	 * steps, so a card can be shown while its frames play.
	 */
	public record RegisterDTO(int register, List<PlayedCardDTO> cards) {

		public record PlayedCardDTO(String username, CardDTO card,
				int firstStep, int stepCount) {
		}

		public static RegisterDTO from(RegisterReveal reveal) {
			return new RegisterDTO(reveal.register(), reveal.cards().stream()
					.map(played -> new PlayedCardDTO(played.username(),
							CardDTO.from(played.card()), played.firstStep(),
							played.stepCount()))
					.toList());
		}
	}

	public static BoardDTO from(Board board) {
		return from(board, List.of());
	}

	public static BoardDTO from(Game game) {
		return from(game, RoundResult.NONE);
	}

	public static BoardDTO from(Game game, RoundResult result) {
		List<RobotDTO> robots = robotsOf(game);
		List<List<RobotDTO>> frames = result.steps().stream()
				.map(frame -> frame.stream().map(RobotDTO::from)
						.sorted(Comparator.comparing(RobotDTO::username))
						.toList())
				.toList();
		List<RegisterDTO> registers = result.registers().stream()
				.map(RegisterDTO::from).toList();
		return from(game.getBoard(), robots, frames, registers);
	}

	private static List<RobotDTO> robotsOf(Game game) {
		return game.getPlayersInGame().stream().map(RobotDTO::from)
				.sorted(Comparator.comparing(RobotDTO::username)).toList();
	}

	private static BoardDTO from(Board board, List<RobotDTO> robots) {
		return from(board, robots, List.of(), List.of());
	}

	private static BoardDTO from(Board board, List<RobotDTO> robots,
			List<List<RobotDTO>> steps, List<RegisterDTO> registers) {
		// Sort spaces by y coordinate, then x coordinate
		List<SpaceDTO> spaces = board.getSpaceMap().values().stream()
				.map(SpaceDTO::from).sorted(Comparator.comparingInt(SpaceDTO::y)
						.thenComparingInt(SpaceDTO::x))
				.toList();

		// Get the width and height of the board or set to 0 if no spaces are
		// found
		int width = spaces.stream().mapToInt(SpaceDTO::x).max().orElse(-1) + 1;
		int height = spaces.stream().mapToInt(SpaceDTO::y).max().orElse(-1) + 1;
		return new BoardDTO(width, height, spaces, robots, steps, registers);
	}
}
