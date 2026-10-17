package dk.dtu.roborally.websocket;

import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.engine.services.LobbyService;
import dk.dtu.roborally.models.lobby.GameLobby;
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
	private final GameService gameService;

	public BoardController(LobbyService lobbyService, GameService gameService) {
		this.lobbyService = lobbyService;
		this.gameService = gameService;
	}

	@SubscribeMapping("/lobby/{lobbyId}/board")
	public BoardDTO board(@DestinationVariable String lobbyId) {
		GameLobby lobby = lobbyService.getLobby(lobbyId);

		if (lobby.getGameId() == null) {
			return BoardDTO.from(lobby.getLevel());
		}

		return BoardDTO.from(gameService.getGame(lobby.getGameId()));
	}
}
