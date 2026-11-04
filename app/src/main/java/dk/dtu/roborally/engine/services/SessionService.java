package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.UserRepository;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// Keeps track of who is logged in. Logging in creates token client send with
// later requests. Sessions live in memory
public class SessionService {
	private final UserRepository userRepository;
	private final SecureRandom random = new SecureRandom();
	private final Map<String, String> sessions = new ConcurrentHashMap<>();

	public SessionService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	// start session for user and get its token
	public String createSession(User user) {
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		String token = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(bytes);
		sessions.put(token, user.getId());
		return token;
	}

	public Optional<User> findUser(String token) {
		if (token == null) {
			return Optional.empty();
		}

		String userId = sessions.get(token);
		if (userId == null) {
			return Optional.empty();
		}

		return userRepository.getById(userId);
	}

	// token stops working, log out
	public void endSession(String token) {
		if (token != null) {
			sessions.remove(token);
		}
	}
}
