package dk.dtu.roborally.api.dto;

import dk.dtu.roborally.loaders.BoardInfo;

/**
 * information the the lobby owner needs to pick an available board.
 *
 * @author August
 */
public record BoardOptionDTO(String id, String name) {

	public static BoardOptionDTO from(BoardInfo board) {
		return new BoardOptionDTO(board.id(), board.name());
	}
}
