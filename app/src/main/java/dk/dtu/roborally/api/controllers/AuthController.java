package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.api.dto.LoginDTO;
import dk.dtu.roborally.api.dto.LoginResponseDTO;
import dk.dtu.roborally.engine.services.UserService;
import dk.dtu.roborally.engine.services.SessionService;
import dk.dtu.roborally.exceptions.auth.LoginException;
import dk.dtu.roborally.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {
	private final UserService userService;
	private final SessionService sessionService;

	public AuthController(UserService userService,
			SessionService sessionService) {
		this.userService = userService;
		this.sessionService = sessionService;

	}

	@GetMapping("/api/session")
	public ResponseEntity<LoginResponseDTO> session(
			@RequestHeader(value = "Authorization", required = false) String authorization) {
		String token = SessionService.tokenFromHeader(authorization);
		User user = sessionService.findUser(token)
				.orElseThrow(() -> new LoginException("Not logged in"));
		return ResponseEntity
				.ok(new LoginResponseDTO(user.getUsername(), token));

	}

	@PostMapping("/api/login")
	public ResponseEntity<LoginResponseDTO> login(
			@RequestBody LoginDTO payload) {
		User user;

		// create user if does not exist
		if (userService.findByUsername(payload.username()).isPresent()) {
			user = userService.authenticate(payload.username(),
					payload.password());
		} else {
			user = userService.addUser(payload.username(), payload.password());
		}

		String token = sessionService.createSession(user);
		return ResponseEntity
				.ok(new LoginResponseDTO(user.getUsername(), token));
	}

	@PostMapping("/api/logout")
	public ResponseEntity<Void> logout(
			@RequestHeader(value = "Authorization", required = false) String authorization) {
		sessionService
				.endSession(SessionService.tokenFromHeader(authorization));
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(LoginException.class)
	public ResponseEntity<Map<String, String>> handleLoginException(
			LoginException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, String>> handleMissingBody(
			HttpMessageNotReadableException ex) {
		return ResponseEntity.badRequest().body(Map.of("error",
				"Request body is required. Send JSON with Content-Type application/json, e.g. {\"username\":\"alice\",\"password\":\"password\"}."));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleInvalidInput(
			IllegalArgumentException ex) {
		return ResponseEntity.badRequest()
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> handleUserNotCreated(
			IllegalStateException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("error", ex.getMessage()));
	}
}
