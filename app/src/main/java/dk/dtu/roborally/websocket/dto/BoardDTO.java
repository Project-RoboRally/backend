package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;

import java.util.Comparator;
import java.util.List;

/**
 * The board layout. Width and height are derived from the furthest space.
 *
 * @author Elias, Nicoleta
 */
public record BoardDTO(int width, int height, List<SpaceDTO> spaces) {

	public record SpaceDTO(int x, int y, List<BoardElementDTO> elements) {

		public static SpaceDTO from(Space space) {
			return new SpaceDTO(space.getPosition().x(),
					space.getPosition().y(), space.getBoardElements().stream()
							.map(BoardElementDTO::from).toList());
		}
	}

	public static BoardDTO from(Board board) {
		// Sort spaces by y coordinate, then x coordinate
		List<SpaceDTO> spaces = board.getSpaceMap().values().stream()
				.map(SpaceDTO::from).sorted(Comparator.comparingInt(SpaceDTO::y)
						.thenComparingInt(SpaceDTO::x))
				.toList();

		// Get the width and height of the board or set to 0 if no spaces are
		// found
		int width = spaces.stream().mapToInt(SpaceDTO::x).max().orElse(-1) + 1;
		int height = spaces.stream().mapToInt(SpaceDTO::y).max().orElse(-1) + 1;
		return new BoardDTO(width, height, spaces);
	}
}
