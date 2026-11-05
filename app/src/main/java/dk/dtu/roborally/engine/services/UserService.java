package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.UserRepository;
import dk.dtu.roborally.exceptions.auth.LoginException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for all user logic
 *
 * @author Elias
 */
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public Optional<User> findByUsername(String username) {
		if (username == null || username.isBlank())
			return Optional.empty();

		return userRepository.getByUsername(username);
	}

	/**
	 * Returns the active user for this username, creating one when the server
	 * does not have them yet. Logging in again with the same username restores
	 * the same user after a restart.
	 */
	public User ensureUser(String username) {
		validateUsername(username);

		Optional<User> existing = userRepository.getByUsername(username);
		if (existing.isPresent())
			return existing.get();

		User user = new User(username);
		if (!userRepository.add(user)) {
			return userRepository.getByUsername(username)
					.orElseThrow(() -> new IllegalStateException(
							"user could not be created"));
		}

		return user;
	}

	// registers a new user, password is stored as a bcrypt hash
	public User addUser(String username, String password) {
		validateUsername(username);
		validatePassword(password);

		if (userRepository.existsByUsername(username))
			throw new IllegalStateException("username already taken");

		// using salted hash
		String passwordHash = passwordEncoder.encode(password);

		User user = new User(UUID.randomUUID().toString(), username,
				passwordHash);
		if (!userRepository.add(user)) {
			throw new IllegalStateException("user could not be created");
		}

		return user;
	}

	private void validateUsername(String username) {
		if (username == null)
			throw new IllegalArgumentException("Username is null");

		if (username.isBlank())
			throw new IllegalArgumentException("Username may not be empty");

		if (username.contains(" "))
			throw new IllegalArgumentException(
					"Username may not contain spaces");

		if (username.length() < 3 || username.length() > 16)
			throw new IllegalArgumentException(
					"Username should be between 3 and 16 characters");
	}

	private void validatePassword(String password) {
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("Password must not be empty");
		}

		if (password.length() < 8) {
			throw new IllegalArgumentException(
					"Password should be at least 8 characters");
		}

		if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new IllegalArgumentException("Password is too long");
		}
	}

	public User authenticate(String username, String password) {
		Optional<User> found = findByUsername(username);

		if (found.isEmpty()) {
			throw new LoginException("Invalid username or password");
		}

		User user = found.get();

		if (password == null || user.getPasswordHash() == null
				|| !passwordEncoder.matches(password, user.getPasswordHash())) {
			throw new LoginException("Invalid username or password");
		}

		return user;
	}

}
