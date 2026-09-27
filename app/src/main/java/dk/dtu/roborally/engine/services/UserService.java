package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.UserRepository;

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

	public void addUser(String username) {
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

		if (userRepository.existsByUsername(username))
			throw new IllegalStateException("username already taken");

		User user = new User(username);
		if (!userRepository.add(user)) {
            throw new IllegalStateException("user could not be created");
        }
	}

}
