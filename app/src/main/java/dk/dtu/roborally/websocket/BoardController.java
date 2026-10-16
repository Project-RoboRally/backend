package dk.dtu.roborally.websocket;

import dk.dtu.roborally.engine.services.LobbyService;
import dk.dtu.roborally.websocket.dto.BoardDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

/**
 * Replies with the lobby's board as soon as a client subscribes to
 * {@code /app/lobby/{lobbyId}/board}, so clients that connect after the game
 * started still receive it.
 *
 * @author Elias, Nicoleta
 */
@Controller
public class BoardController {

	private final LobbyService lobbyService;

	public BoardController(LobbyService lobbyService) {
		this.lobbyService = lobbyService;
	}

	@SubscribeMapping("/lobby/{lobbyId}/board")
	public BoardDTO board(@DestinationVariable String lobbyId) {
		return BoardDTO.from(lobbyService.getLobby(lobbyId).getLevel());
	}
}
