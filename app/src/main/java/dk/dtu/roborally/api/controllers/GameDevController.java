package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.enums.GameStatus;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

/**
 * Development-only controller for manually changing game state.
 *
 * ## Test curls on Linux/Mac (Use fx Postman or Insomnia on Windows):
 *
 * # Get current game state curl -i \ http://localhost:8080/api/games/{gameId}
 *
 * # Set game status to FINISHED curl -i -X PATCH \
 * http://localhost:8080/api/dev/games/{gameId}/status \ -H "Content-Type:
 * application/json" \ -d '{"status":"FINISHED"}'
 *
 * # Set game status to IN_PROGRESS curl -i -X PATCH \
 * http://localhost:8080/api/dev/games/{gameId}/status \ -H "Content-Type:
 * application/json" \ -d '{"status":"IN_PROGRESS"}'
 *
 * @author Matthias
 */
@RestController
@RequestMapping("/api/dev/games")
@Profile("dev")
public class GameDevController {

	private final GameService gameService;

	public GameDevController(GameService gameService) {
		this.gameService = gameService;
	}

	@PatchMapping("/{id}/status")
	public void updateStatus(@PathVariable String id,
			@RequestBody GameStatusRequest request) {

		gameService.updateStatus(id, request.status());
	}

	public record GameStatusRequest(GameStatus status) {
	}
}
