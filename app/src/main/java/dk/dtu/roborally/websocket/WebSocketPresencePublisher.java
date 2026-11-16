package dk.dtu.roborally.websocket;

import dk.dtu.roborally.engine.publishers.PresencePublisher;
import dk.dtu.roborally.models.game.KickVote;
import dk.dtu.roborally.models.game.PlayerPresence;
import dk.dtu.roborally.websocket.dto.PresenceDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Broadcasts player presence to {@code /game/{gameId}/presence}.
 *
 * @author Elias
 */
@Component
public class WebSocketPresencePublisher implements PresencePublisher {

	private final SimpMessagingTemplate messagingTemplate;

	public WebSocketPresencePublisher(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public static String presenceTopic(String gameId) {
		return "/game/" + gameId + "/presence";
	}

	@Override
	public void publishPresence(String gameId, List<PlayerPresence> players,
			List<String> ask, KickVote vote) {
		messagingTemplate.convertAndSend(presenceTopic(gameId),
				new PresenceDTO(players, ask, vote));
	}
}
