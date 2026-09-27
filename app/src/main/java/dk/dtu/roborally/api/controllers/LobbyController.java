package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.api.dto.*;
import dk.dtu.roborally.api.services.ILobbyService;
import dk.dtu.roborally.exceptions.lobby.LobbyNotFoundException;
import dk.dtu.roborally.exceptions.lobby.NotAuthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handles lobby creation, retrieval, and player joining, leaving, kicking other players
 *
 * @author Nicoleta
 */

@RestController
@RequestMapping("/api/lobbies")
public class LobbyController {
	private final ILobbyService lobbyService;

	public LobbyController(ILobbyService lobbyService) {
		this.lobbyService = lobbyService;
	}

	@GetMapping
	public ResponseEntity<List<LobbyDTO>> getLobbies() {
		return ResponseEntity.ok(lobbyService.getLobbies());
	}

	@GetMapping("/{id}")
	public ResponseEntity<LobbyDTO> getLobby(@PathVariable String id) {
		return ResponseEntity.ok(lobbyService.getLobby(id));
	}

	@PostMapping
	public ResponseEntity<LobbyDTO> createLobby(@RequestBody CreateLobbyDTO payload) {
		return ResponseEntity.ok(lobbyService.createLobby(payload));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<LobbyDTO> updateLobby(@PathVariable String id, @RequestBody RenameLobbyDTO payload) {
		return ResponseEntity.ok(lobbyService.renameLobby(id, payload.name()));
	}

	@PostMapping("/{id}/join")
	public ResponseEntity<LobbyDTO> joinLobby(@PathVariable String id, @RequestBody AddToRemoveFromLobbyDTO payload) {
		return ResponseEntity.ok(lobbyService.joinLobby(id, payload.username()));
	}

	@PostMapping("/{id}/leave")
	public ResponseEntity<LobbyDTO> leaveLobby(@PathVariable String id, @RequestBody AddToRemoveFromLobbyDTO payload) {
		return ResponseEntity.ok(lobbyService.leaveLobby(id, payload.username()));
	}

	@PostMapping("/{id}/kick")
	public ResponseEntity<LobbyDTO> kickPlayer(@PathVariable String id, @RequestBody KickPlayerDTO payload) {
		return ResponseEntity.ok(lobbyService.kickPlayer(id, payload.kickedBy(), payload.playerKicked()));
	}

	@ExceptionHandler(LobbyNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(LobbyNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(NotAuthorizedException.class)
	public ResponseEntity<Map<String, String>> handleNotAuthorized(NotAuthorizedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
	}
}
