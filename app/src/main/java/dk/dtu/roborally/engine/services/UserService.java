package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.UserRepository;

import java.util.Optional;

/**
 * Service for all user logic
 *
 * @author Elias
 */
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
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

	public void addUser(String username) {
		validateUsername(username);

		if (userRepository.existsByUsername(username))
			throw new IllegalStateException("username already taken");

		User user = new User(username);
		if (!userRepository.add(user)) {
			throw new IllegalStateException("user could not be created");
		}
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

}
