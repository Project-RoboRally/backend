package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.api.dto.*;
import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.engine.services.SessionService;
import dk.dtu.roborally.exceptions.auth.LoginException;
import dk.dtu.roborally.exceptions.game.GameNotFoundException;
import dk.dtu.roborally.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles the decisions a player makes during a running game: programming,
 * upgrades, putting cards back, and choices during activation.
 *
 * @author Nicoleta, Elias
 */

@RestController
@RequestMapping("/api/games")
public class GameController {
	private final GameService gameService;
	private final SessionService sessionService;

	public GameController(GameService gameService,
			SessionService sessionService) {
		this.gameService = gameService;
		this.sessionService = sessionService;
	}

	private String currentUsername(String authorization) {
		return sessionService
				.findUser(SessionService.tokenFromHeader(authorization))
				.map(User::getUsername)
				.orElseThrow(() -> new LoginException("Not logged in"));
	}

	@PostMapping("/{id}/program")
	public ResponseEntity<ProgramResultDTO> program(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody ProgramDTO payload) {
		return ResponseEntity
				.ok(new ProgramResultDTO(gameService.submitProgram(id,
						currentUsername(authorization), payload.registers())));
	}

	@PostMapping("/{id}/upgrade/buy")
	public ResponseEntity<Void> buyUpgrade(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody BuyUpgradeDTO payload) {
		gameService.buyUpgrade(id, currentUsername(authorization),
				payload.upgrade(), payload.replace());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/upgrade/skip")
	public ResponseEntity<Void> skipUpgrade(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization) {
		gameService.skipUpgrade(id, currentUsername(authorization));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/upgrade/use")
	public ResponseEntity<Void> useUpgrade(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody UseUpgradeDTO payload) {
		gameService.useUpgrade(id, currentUsername(authorization),
				payload.upgrade(), payload.cards());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/hand/put-back")
	public ResponseEntity<Void> putBackCards(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody PutBackCardsDTO payload) {
		gameService.putBackCards(id, currentUsername(authorization),
				payload.cards());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/choice")
	public ResponseEntity<Void> makeChoice(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody MakeChoiceDTO payload) {
		gameService.makeChoice(id, currentUsername(authorization),
				payload.choice());
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(LoginException.class)
	public ResponseEntity<Map<String, String>> handleNotLoggedIn(
			LoginException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(GameNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(
			GameNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> handleIllegalState(
			IllegalStateException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleIllegalArgument(
			IllegalArgumentException ex) {
		return ResponseEntity.badRequest()
				.body(Map.of("error", ex.getMessage()));
	}
}
