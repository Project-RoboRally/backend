package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.api.dto.*;
import dk.dtu.roborally.engine.services.LobbyService;
import dk.dtu.roborally.engine.services.SessionService;
import dk.dtu.roborally.exceptions.auth.LoginException;
import dk.dtu.roborally.exceptions.lobby.*;
import dk.dtu.roborally.exceptions.user.UserNotFound;
import dk.dtu.roborally.models.User;
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
	private final SessionService sessionService;

	public LobbyController(LobbyService lobbyService,
			SessionService sessionService) {
		this.lobbyService = lobbyService;
		this.sessionService = sessionService;
	}

	private String currentUsername(String authorization) {
		return sessionService
				.findUser(SessionService.tokenFromHeader(authorization))
				.map(User::getUsername)
				.orElseThrow(() -> new LoginException("Not logged in"));
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
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody CreateLobbyDTO payload) {
		CreateLobbyDTO request = new CreateLobbyDTO(payload.name(),
				currentUsername(authorization));
		return ResponseEntity
				.ok(LobbyDTO.from(lobbyService.createLobby(request)));
	}

	@PostMapping("/{id}/rename")
	public ResponseEntity<LobbyDTO> updateLobby(@PathVariable String id,
			@RequestBody RenameLobbyDTO payload) {
		return ResponseEntity.ok(
				LobbyDTO.from(lobbyService.renameLobby(id, payload.name())));
	}

	@PostMapping("/{id}/join")
	public ResponseEntity<LobbyDTO> joinLobby(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization) {
		return ResponseEntity.ok(LobbyDTO.from(
				lobbyService.joinLobby(id, currentUsername(authorization))));
	}

	@PostMapping("/{id}/leave")
	public ResponseEntity<LobbyDTO> leaveLobby(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization) {
		return ResponseEntity.ok(LobbyDTO.from(
				lobbyService.leaveLobby(id, currentUsername(authorization))));
	}

	@PostMapping("/{id}/kick")
	public ResponseEntity<LobbyDTO> kickUser(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody KickUserDTO payload) {
		lobbyService.kickUser(id, currentUsername(authorization),
				payload.userKicked());
		return ResponseEntity.ok(LobbyDTO.from(lobbyService.getLobby(id)));
	}

	@PostMapping("/{id}/unkick")
	public ResponseEntity<LobbyDTO> unKickUser(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody KickUserDTO payload) {
		lobbyService.unKickUser(id, currentUsername(authorization),
				payload.userKicked());
		return ResponseEntity.ok(LobbyDTO.from(lobbyService.getLobby(id)));
	}

	@PostMapping("/{id}/ready")
	public ResponseEntity<LobbyDTO> setReady(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody SetReadyDTO payload) {
		return ResponseEntity.ok(LobbyDTO.from(lobbyService.setReady(id,
				currentUsername(authorization), payload.ready())));
	}

	@PostMapping("/{id}/color")
	public ResponseEntity<LobbyDTO> setColor(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody SetColorDTO payload) {
		return ResponseEntity.ok(LobbyDTO.from(lobbyService.setColor(id,
				currentUsername(authorization), payload.color())));
	}

	@PostMapping("/{id}/start")
	public ResponseEntity<LobbyDTO> startGame(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization) {
		return ResponseEntity.ok(LobbyDTO.from(
				lobbyService.startGame(id, currentUsername(authorization))));
	}

	@PostMapping("/{id}/program")
	public ResponseEntity<ProgramResultDTO> program(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody ProgramDTO payload) {
		return ResponseEntity
				.ok(new ProgramResultDTO(lobbyService.submitProgram(id,
						currentUsername(authorization), payload.registers())));
	}

	@PostMapping("/{id}/upgrade/buy")
	public ResponseEntity<Void> buyUpgrade(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody BuyUpgradeDTO payload) {
		lobbyService.buyUpgrade(id, currentUsername(authorization),
				payload.upgrade(), payload.replace());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/upgrade/skip")
	public ResponseEntity<Void> skipUpgrade(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization) {
		lobbyService.skipUpgrade(id, currentUsername(authorization));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/upgrade/use")
	public ResponseEntity<Void> useUpgrade(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody UseUpgradeDTO payload) {
		lobbyService.useUpgrade(id, currentUsername(authorization),
				payload.upgrade(), payload.cards());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/hand/put-back")
	public ResponseEntity<Void> putBackCards(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody PutBackCardsDTO payload) {
		lobbyService.putBackCards(id, currentUsername(authorization),
				payload.cards());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/choice")
	public ResponseEntity<Void> makeChoice(@PathVariable String id,
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@RequestBody MakeChoiceDTO payload) {
		lobbyService.makeChoice(id, currentUsername(authorization),
				payload.choice());
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(LoginException.class)
	public ResponseEntity<Map<String, String>> handleNotLoggedIn(
			LoginException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(LobbyNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(
			LobbyNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler({UserNotFound.class, LobbyCreatorNotFoundOnCreate.class})
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

	@ExceptionHandler(LobbyUserAlreadyInLobby.class)
	public ResponseEntity<Map<String, String>> handleAlreadyInLobby(
			LobbyUserAlreadyInLobby ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(UserNotInLobby.class)
	public ResponseEntity<Map<String, String>> handlePlayerNotInLobby(
			UserNotInLobby ex) {
		return ResponseEntity.badRequest()
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
