package dk.dtu.roborally.websocket;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.RoundResult;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.websocket.dto.BoardDTO;
import dk.dtu.roborally.websocket.dto.GameStatusDTO;
import dk.dtu.roborally.websocket.dto.PlayerGameStateDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Broadcasts game events to subscribers over WebSocket.
 *
 * @author Nicoleta, Elias, Matthias, Sebastian
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

	public static String playerTopic(String gameId, String username) {
		return "/game/" + gameId + "/player/" + username;
	}

	public static String boardTopic(String gameId) {
		return "/game/" + gameId + "/board";
	}

	@Override
	public void publishBoard(Game game) {
		publishRound(game, RoundResult.NONE);
	}

	@Override
	public void publishRound(Game game, RoundResult result) {
		messagingTemplate.convertAndSend(boardTopic(game.getGameID()),
				BoardDTO.from(game, result));

		for (Player player : game.getPlayersInGame()) {
			messagingTemplate.convertAndSend(
					playerTopic(game.getGameID(),
							player.getUser().getUsername()),
					PlayerGameStateDTO.from(PlayerGameStateDTO.CURRENT_STATE,
							game, player, result));
		}
	}

	@Override
	public void publishStatus(Game game) {
		messagingTemplate.convertAndSend(statusTopic(game.getGameID()),
				GameStatusDTO.from(game));
	}

	@Override
	public void publishGameStarted(Game game) {
		publishPlayerStates(game, PlayerGameStateDTO.GAME_STARTED);
	}

	@Override
	public void publishRoundStarted(Game game) {
		publishPlayerStates(game, PlayerGameStateDTO.ROUND_STARTED);
	}

	@Override
	public void publishUpgradeTurn(Game game) {
		publishPlayerStates(game, PlayerGameStateDTO.UPGRADE_TURN);
	}

	@Override
	public void publishProgrammingStarted(Game game) {
		publishPlayerStates(game, PlayerGameStateDTO.PROGRAMMING_STARTED);
	}

	@Override
	public void publishUpgradeUsed(Game game) {
		publishPlayerStates(game, PlayerGameStateDTO.UPGRADE_USED);
	}

	@Override
	public void publishTimerStarted(Game game) {
		publishPlayerStates(game, PlayerGameStateDTO.TIMER_STARTED);
	}

	@Override
	public void publishChoiceRequired(Game game) {
		publishPlayerStates(game, PlayerGameStateDTO.CHOICE_REQUIRED);
	}

	private void publishPlayerStates(Game game, String type) {
		// Each player gets their own message since it holds their private mat
		for (Player player : game.getPlayersInGame()) {
			messagingTemplate.convertAndSend(
					playerTopic(game.getGameID(),
							player.getUser().getUsername()),
					PlayerGameStateDTO.from(type, game, player));
		}
	}
}
