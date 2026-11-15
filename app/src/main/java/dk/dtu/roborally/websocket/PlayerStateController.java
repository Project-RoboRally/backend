package dk.dtu.roborally.websocket;

import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.websocket.dto.PlayerGameStateDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

/**
 * Replies with a player's current game state as soon as they subscribe to
 * /app/game/{gameId}/player/{username}, so a player who opens or reloads the
 * game page after it started still receives it.
 *
 * @author Nicoleta, Elias
 */
@Controller
public class PlayerStateController {

	private final GameService gameService;

	public PlayerStateController(GameService gameService) {
		this.gameService = gameService;
	}

	// Returns null, so nothing is sent, while the user is not a player in the
	// game.
	@SubscribeMapping("/game/{gameId}/player/{username}")
	public PlayerGameStateDTO playerState(@DestinationVariable String gameId,
			@DestinationVariable String username) {
		Game game = gameService.getGame(gameId);
		for (Player player : game.getPlayersInGame()) {
			if (player.getUser().getUsername().equals(username)) {
				return PlayerGameStateDTO.from(PlayerGameStateDTO.CURRENT_STATE,
						game, player);
			}
		}
		return null;
	}
}
