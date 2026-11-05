package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.UserRepository;
import dk.dtu.roborally.exceptions.auth.LoginException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Service for all user logic
 *
 * @author Elias, Anton
 */
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder = Argon2PasswordEncoder
			.defaultsForSpringSecurity_v5_8();

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public Optional<User> findByUsername(String username) {
		if (username == null || username.isBlank())
			return Optional.empty();

		return userRepository.getByUsername(username);
	}

	public User register(String username, String password) {
		validateUsername(username);
		validatePassword(password);

		User user = new User(username, passwordEncoder.encode(password));
		if (!userRepository.add(user)) {
			throw new IllegalStateException("username already taken");
		}
		return user;
	}

	public User authenticate(String username, String password) {
		User user = findByUsername(username).orElseThrow(
				() -> new LoginException("Invalid username or password"));
		if (password == null || user.getPasswordHash() == null
				|| !passwordEncoder.matches(password, user.getPasswordHash())) {
			throw new LoginException("Invalid username or password");
		}
		return user;
	}

	private void validateUsername(String username) {
		if (username == null)
			throw new IllegalArgumentException("Username is null");

		if (username.isBlank())
			throw new IllegalArgumentException("Username may not be empty");

		if (username.chars().anyMatch(Character::isWhitespace))
			throw new IllegalArgumentException(
					"Username may not contain spaces");

		if (username.length() < 3 || username.length() > 16)
			throw new IllegalArgumentException(
					"Username should be between 3 and 16 characters");
	}

	private void validatePassword(String password) {
		if (password == null || password.length() < 8)
			throw new IllegalArgumentException(
					"Password must be at least 8 characters");

		if (password.getBytes(StandardCharsets.UTF_8).length > 72)
			throw new IllegalArgumentException(
					"Password must be no more than 72 UTF-8 bytes");
	}

}
