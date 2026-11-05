package dk.dtu.roborally.websocket;

import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.engine.services.LobbyService;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.websocket.dto.PlayerGameStateDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Replies with a player's current game state as soon as they subscribe to
 * /app/lobby/{lobbyId}/player/{username}, so a player who opens or reloads the
 * game page after it started still receives it.
 *
 * @author Nicoleta, Anton
 */
@Controller
public class PlayerStateController {

	private final LobbyService lobbyService;
	private final GameService gameService;

	public PlayerStateController(LobbyService lobbyService,
			GameService gameService) {
		this.lobbyService = lobbyService;
		this.gameService = gameService;
	}

	// Returns null, so nothing is sent, while the game has not started or the
	// user is not a player in it.
	@SubscribeMapping("/lobby/{lobbyId}/player/{username}")
	public PlayerGameStateDTO playerState(@DestinationVariable String lobbyId,
			@DestinationVariable String username, Principal principal) {
		if (principal == null || !principal.getName().equals(username)) {
			throw new AccessDeniedException(
					"Players may only access their own game state");
		}
		return currentState(lobbyId, username);
	}

	public PlayerGameStateDTO playerState(String lobbyId, String username) {
		return currentState(lobbyId, username);
	}

	private PlayerGameStateDTO currentState(String lobbyId, String username) {
		GameLobby lobby = lobbyService.getLobby(lobbyId);

		if (lobby.getGameId() == null) {
			return null;
		}

		Game game = gameService.getGame(lobby.getGameId());
		for (Player player : game.getPlayersInGame()) {
			if (player.getUser().getUsername().equals(username)) {
				return PlayerGameStateDTO.from(PlayerGameStateDTO.CURRENT_STATE,
						game, player);
			}
		}
		return null;
	}
}
