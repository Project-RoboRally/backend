package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.models.game.Game;

/**
 * WebSocket payload sent when the status of a game changes.
 *
 * @author Nicoleta
 */
public record GameStatusDTO(String gameId, GameStatus status) {

	public static GameStatusDTO from(Game game) {
		return new GameStatusDTO(game.getGameID(), game.getGameStatus());
	}
}
