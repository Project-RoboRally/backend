package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.models.game.Game;

/**
 * WebSocket payload sent when the status of a game changes.
 *
 * @author Nicoleta, Matthias
 */
public record GameStatusDTO(String gameId, GameStatus status,
		String winnerUsername) {
	public static GameStatusDTO from(Game game) {
		String winnerUsername = game.getWinner() == null
				? null
				: game.getWinner().getUser().getUsername();

		return new GameStatusDTO(game.getGameID(), game.getGameStatus(),
				winnerUsername);
	}
}
