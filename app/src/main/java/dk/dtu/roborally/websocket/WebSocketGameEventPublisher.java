package dk.dtu.roborally.websocket;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.RobotView;
import dk.dtu.roborally.websocket.dto.BoardDTO;
import dk.dtu.roborally.websocket.dto.GameStatusDTO;
import dk.dtu.roborally.websocket.dto.PlayerGameStateDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Broadcasts game events to subscribers over WebSocket.
 *
 * @author Nicoleta, Elias, Sebastian
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
	public void publishBoard(String lobbyId, Game game) {
		publishRound(lobbyId, game, List.of());
	}

	@Override
	public void publishRound(String lobbyId, Game game,
			List<List<RobotView>> steps) {
		messagingTemplate.convertAndSend(boardTopic(lobbyId),
				BoardDTO.from(game, steps));
	}

	@Override
	public void publishStatus(Game game) {
		messagingTemplate.convertAndSend(statusTopic(game.getGameID()),
				GameStatusDTO.from(game));
	}

	@Override
	public void publishGameStarted(String lobbyId, Game game) {
		publishPlayerStates(lobbyId, game, PlayerGameStateDTO.GAME_STARTED);
	}

	@Override
	public void publishRoundStarted(String lobbyId, Game game) {
		publishPlayerStates(lobbyId, game, PlayerGameStateDTO.ROUND_STARTED);
	}

	private void publishPlayerStates(String lobbyId, Game game, String type) {
		// Each player gets their own message since it holds their private mat
		for (Player player : game.getPlayersInGame()) {
			messagingTemplate.convertAndSend(
					playerTopic(lobbyId, player.getUser().getUsername()),
					PlayerGameStateDTO.from(type, game, player));
		}
	}
}
