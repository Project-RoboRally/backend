package dk.dtu.roborally.websocket;

import dk.dtu.roborally.engine.services.PresenceService;
import dk.dtu.roborally.websocket.dto.PresenceDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

/**
 * Receives health pings on {@code /app/health/{gameId}} and replies with the
 * current presence when a client subscribes to
 * {@code /app/game/{gameId}/presence}. Also runs the presence tick.
 *
 * @author Elias
 */
@Controller
public class PresenceController {

	private final PresenceService presenceService;

	public PresenceController(PresenceService presenceService) {
		this.presenceService = presenceService;
	}

	@MessageMapping("/health/{gameId}")
	public void health(@DestinationVariable String gameId,
			Principal principal) {
		if (principal != null) {
			presenceService.recordPing(gameId, principal.getName());
		}
	}

	@SubscribeMapping("/game/{gameId}/presence")
	public PresenceDTO presence(@DestinationVariable String gameId) {
		return new PresenceDTO(presenceService.presence(gameId), List.of(),
				presenceService.vote(gameId));
	}

	@Scheduled(fixedRate = PresenceService.TICK_MS)
	public void tick() {
		presenceService.tick();
	}
}
