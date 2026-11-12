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
import dk.dtu.roborally.engine.services.LoginAttemptService;
import dk.dtu.roborally.exceptions.auth.TooManyLoginAttemptsException;

import java.util.Map;

/**
 * Login, logout and session checks. Issues the session tokens that the rest of
 * the API uses to know who is calling.
 *
 * @author Nicoleta
 */

@RestController
public class AuthController {
	private final UserService userService;
	private final SessionService sessionService;
	private final LoginAttemptService loginAttemptService;

	public AuthController(UserService userService,
			SessionService sessionService,
			LoginAttemptService loginAttemptService) {
		this.userService = userService;
		this.sessionService = sessionService;
		this.loginAttemptService = loginAttemptService;

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
		String username = payload.username();
		if (loginAttemptService.isBlocked(username)) {
			throw new TooManyLoginAttemptsException();
		}

		User user;

		// create user if does not exist
		if (userService.findByUsername(username).isPresent()) {
			try {
				user = userService.authenticate(username, payload.password());
			} catch (LoginException ex) {
				loginAttemptService.recordFailure(username);
				throw ex;
			}
			loginAttemptService.recordSuccess(username);
		} else {
			user = userService.addUser(username, payload.password());
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

	@ExceptionHandler(TooManyLoginAttemptsException.class)
	public ResponseEntity<Map<String, String>> handleTooManyAttempts(
			TooManyLoginAttemptsException ex) {
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
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
