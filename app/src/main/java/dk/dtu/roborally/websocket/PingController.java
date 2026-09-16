package dk.dtu.roborally.websocket;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.Map;

@Controller
public class PingController {

	@MessageMapping("/ping/{gameId}")
	@SendTo("/topic/pong/{gameId}")
	public Map<String, Object> ping(@DestinationVariable String gameId) {
		return Map.of("message", "pong", "gameId", gameId, "timestamp",
				Instant.now().toString());
	}
}
