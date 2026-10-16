package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.api.dto.*;
import dk.dtu.roborally.engine.services.LobbyService;
import dk.dtu.roborally.exceptions.lobby.LobbyCreatorNotFoundOnCreate;
import dk.dtu.roborally.exceptions.lobby.LobbyInvalidName;
import dk.dtu.roborally.exceptions.lobby.LobbyNotFoundException;
import dk.dtu.roborally.exceptions.lobby.LobbyPlayerAlreadyInLobby;
import dk.dtu.roborally.exceptions.lobby.NotAuthorizedException;
import dk.dtu.roborally.exceptions.lobby.PlayerNotInLobby;
import dk.dtu.roborally.exceptions.player.PlayerNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handles lobby creation, retrieval, and player joining, leaving, kicking other
 * players
 *
 * @author Nicoleta
 */

@RestController
@RequestMapping("/api/lobbies")
public class LobbyController {
	private final LobbyService lobbyService;

	public LobbyController(LobbyService lobbyService) {
		this.lobbyService = lobbyService;
	}

	@GetMapping
	public ResponseEntity<List<LobbyDTO>> getLobbies() {
		return ResponseEntity.ok(lobbyService.getLobbies().stream()
				.map(LobbyDTO::from).toList());
	}

	@GetMapping("/{id}")
	public ResponseEntity<LobbyDTO> getLobby(@PathVariable String id) {
		return ResponseEntity.ok(LobbyDTO.from(lobbyService.getLobby(id)));
	}

	@PostMapping
	public ResponseEntity<LobbyDTO> createLobby(
			@RequestBody CreateLobbyDTO payload) {
		return ResponseEntity
				.ok(LobbyDTO.from(lobbyService.createLobby(payload)));
	}

	@PostMapping("/{id}/rename")
	public ResponseEntity<LobbyDTO> updateLobby(@PathVariable String id,
			@RequestBody RenameLobbyDTO payload) {
		return ResponseEntity.ok(
				LobbyDTO.from(lobbyService.renameLobby(id, payload.name())));
	}

	@PostMapping("/{id}/join")
	public ResponseEntity<LobbyDTO> joinLobby(@PathVariable String id,
			@RequestBody AddToRemoveFromLobbyDTO payload) {
		return ResponseEntity.ok(
				LobbyDTO.from(lobbyService.joinLobby(id, payload.username())));
	}

	@PostMapping("/{id}/leave")
	public ResponseEntity<LobbyDTO> leaveLobby(@PathVariable String id,
			@RequestBody AddToRemoveFromLobbyDTO payload) {
		return ResponseEntity.ok(
				LobbyDTO.from(lobbyService.leaveLobby(id, payload.username())));
	}

	@PostMapping("/{id}/start")
	public ResponseEntity<LobbyDTO> startGame(@PathVariable String id,
			@RequestBody AddToRemoveFromLobbyDTO payload) {
		return ResponseEntity.ok(
				LobbyDTO.from(lobbyService.startGame(id, payload.username())));
	}

	@PostMapping("/{id}/kick")
	public ResponseEntity<LobbyDTO> kickUser(@PathVariable String id,
			@RequestBody KickUserDTO payload) {
		lobbyService.kickUser(id, payload.kickedBy(), payload.userKicked());
		return ResponseEntity.ok(LobbyDTO.from(lobbyService.getLobby(id)));
	}

	@ExceptionHandler(LobbyNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(
			LobbyNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler({PlayerNotFound.class,
			LobbyCreatorNotFoundOnCreate.class})
	public ResponseEntity<Map<String, String>> handlePlayerMissing(
			RuntimeException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(NotAuthorizedException.class)
	public ResponseEntity<Map<String, String>> handleNotAuthorized(
			NotAuthorizedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(LobbyInvalidName.class)
	public ResponseEntity<Map<String, String>> handleInvalidName(
			LobbyInvalidName ex) {
		return ResponseEntity.badRequest()
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(LobbyPlayerAlreadyInLobby.class)
	public ResponseEntity<Map<String, String>> handleAlreadyInLobby(
			LobbyPlayerAlreadyInLobby ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(PlayerNotInLobby.class)
	public ResponseEntity<Map<String, String>> handlePlayerNotInLobby(
			PlayerNotInLobby ex) {
		return ResponseEntity.badRequest()
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> handleIllegalState(
			IllegalStateException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("error", ex.getMessage()));
	}
}
