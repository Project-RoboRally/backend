package dk.dtu.roborally.websocket;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.websocket.dto.BoardDTO;
import dk.dtu.roborally.websocket.dto.GameStatusDTO;
import dk.dtu.roborally.websocket.dto.PlayerGameStateDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Broadcasts game events to subscribers over WebSocket.
 *
 * @author Nicoleta, Elias
 */
@Component
public class WebSocketGameEventPublisher implements GameEventPublisher {

	private final SimpMessagingTemplate messagingTemplate;

	public WebSocketGameEventPublisher(
			SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public static String statusTopic(String gameId) {
		return "/game/" + gameId + "/status";
	}

	public static String playerTopic(String lobbyId, String username) {
		return "/game/lobby/" + lobbyId + "/player/" + username;
	}

	public static String boardTopic(String lobbyId) {
		return "/game/lobby/" + lobbyId + "/board";
	}

	@Override
	public void publishBoard(String lobbyId, Board board) {
		messagingTemplate.convertAndSend(boardTopic(lobbyId),
				BoardDTO.from(board));
	}

	@Override
	public void publishStatus(Game game) {
		messagingTemplate.convertAndSend(statusTopic(game.getGameID()),
				GameStatusDTO.from(game));
	}

	@Override
	public void publishGameStarted(String lobbyId, Game game) {
		// Publish the game started event to all players in the game
		for (Player player : game.getPlayersInGame()) {
			messagingTemplate.convertAndSend(
					playerTopic(lobbyId, player.getUser().getUsername()),
					PlayerGameStateDTO.from(PlayerGameStateDTO.GAME_STARTED,
							game, player));
		}
	}
}
