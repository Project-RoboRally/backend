package dk.dtu.roborally.websocket;

import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.websocket.dto.BoardDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

/**
 * Replies with the game's board as soon as a client subscribes to
 * {@code /app/game/{gameId}/board}, so clients that connect after the game
 * started still receive it.
 *
 * @author Elias, Nicoleta
 */
@Controller
public class BoardController {

	private final GameService gameService;

	public BoardController(GameService gameService) {
		this.gameService = gameService;
	}

	@SubscribeMapping("/game/{gameId}/board")
	public BoardDTO board(@DestinationVariable String gameId) {
		return BoardDTO.from(gameService.getGame(gameId));
	}
}
