package dk.dtu.roborally.websocket;

import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.websocket.dto.GameStatusDTO;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.mockito.Mockito.*;

class StompGameEventPublisherTest {

	@Test
	void publishesStatusToGameTopic() {
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		StompGameEventPublisher publisher = new StompGameEventPublisher(
				template);
		Game game = new Game("game-1", null, Map.of());
		game.setGameStatus(GameStatus.FINISHED);

		publisher.publishStatus(game);

		verify(template).convertAndSend("/topic/game/game-1/status",
				new GameStatusDTO("game-1", GameStatus.FINISHED));
	}
}
