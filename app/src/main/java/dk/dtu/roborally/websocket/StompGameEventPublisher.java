package dk.dtu.roborally.websocket;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.websocket.dto.GameStatusDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Broadcasts game events to subscribers over STOMP.
 *
 * @author Nicoleta
 */
@Component
public class StompGameEventPublisher implements GameEventPublisher {

	private final SimpMessagingTemplate messagingTemplate;

	public StompGameEventPublisher(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public static String statusTopic(String gameId) {
		return "/topic/game/" + gameId + "/status";
	}

	@Override
	public void publishStatus(Game game) {
		messagingTemplate.convertAndSend(statusTopic(game.getGameID()),
				GameStatusDTO.from(game));
	}
}
