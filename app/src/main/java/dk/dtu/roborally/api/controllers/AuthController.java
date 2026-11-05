package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.api.dto.LoginDTO;
import dk.dtu.roborally.api.dto.LoginResponseDTO;
import dk.dtu.roborally.engine.services.UserService;
import dk.dtu.roborally.exceptions.auth.LoginException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Handles account registration, login, and session details.
 *
 * @author Anton
 */
@RestController
public class AuthController {
	private final UserService userService;
	private final SecurityContextRepository securityContextRepository;

	public AuthController(UserService userService,
			SecurityContextRepository securityContextRepository) {
		this.userService = userService;
		this.securityContextRepository = securityContextRepository;
	}

	@GetMapping("/api/session")
	public LoginResponseDTO session(Principal principal) {
		return new LoginResponseDTO(principal.getName());
	}

	@PostMapping("/api/login")
	public ResponseEntity<?> login(@RequestBody LoginDTO payload,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			if (payload == null)
				throw new LoginException("Invalid username or password");
			var user = userService.authenticate(payload.username(),
					payload.password());
			authenticate(user.getUsername(), request, response);
			return ResponseEntity.ok(new LoginResponseDTO(user.getUsername()));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", ex.getMessage()));
		}
	}

	@PostMapping("/api/register")
	public ResponseEntity<?> register(@RequestBody LoginDTO payload,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			if (payload == null)
				throw new IllegalArgumentException(
						"Username and password are required");
			var user = userService.register(payload.username(),
					payload.password());
			authenticate(user.getUsername(), request, response);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(new LoginResponseDTO(user.getUsername()));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest()
					.body(Map.of("error", ex.getMessage()));
		} catch (IllegalStateException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("error", ex.getMessage()));
		}
	}

	@GetMapping("/api/csrf")
	public CsrfToken csrf(CsrfToken csrfToken) {
		return csrfToken;
	}

	private void authenticate(String username, HttpServletRequest request,
			HttpServletResponse response) {
		request.getSession(true);
		request.changeSessionId();

		Authentication authentication = UsernamePasswordAuthenticationToken
				.authenticated(username, null, List.of());
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, request, response);
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
}
