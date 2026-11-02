package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.api.dto.LoginDTO;
import dk.dtu.roborally.api.dto.LoginResponseDTO;
import dk.dtu.roborally.engine.services.UserService;
import dk.dtu.roborally.exceptions.auth.LoginException;
import dk.dtu.roborally.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {
	private final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/api/session")
	public ResponseEntity<?> session(@RequestParam String username) {
		return userService.findByUsername(username)
				.<ResponseEntity<?>>map(user -> ResponseEntity
						.ok(new LoginResponseDTO(user.getUsername())))
				.orElseGet(() -> ResponseEntity.notFound().build());
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

		return ResponseEntity.ok(new LoginResponseDTO(user.getUsername()));
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
