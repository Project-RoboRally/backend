package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.enums.GameStatus;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

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
