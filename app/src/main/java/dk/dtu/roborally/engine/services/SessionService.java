package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.UserRepository;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import dk.dtu.roborally.engine.GameClock;
import dk.dtu.roborally.engine.SystemGameClock;
import java.time.Duration;
import java.time.Instant;

// Keeps track of who is logged in. Logging in creates token client send with
// later requests. Sessions live in memory and expire after 24 hours
public class SessionService {
	private static final Duration DEFAULT_LIFETIME = Duration.ofHours(24);

	private final UserRepository userRepository;
	private final GameClock clock;
	private final Duration lifetime;
	private final SecureRandom random = new SecureRandom();
	private final Map<String, Session> sessions = new ConcurrentHashMap<>();

	private record Session(String userId, Instant expiresAt) {
	}

	public SessionService(UserRepository userRepository) {
		this(userRepository, new SystemGameClock(), DEFAULT_LIFETIME);
	}

	public SessionService(UserRepository userRepository, GameClock clock,
			Duration lifetime) {
		this.userRepository = userRepository;
		this.clock = clock;
		this.lifetime = lifetime;
	}

	// start session for user and get its token
	public String createSession(User user) {
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		String token = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(bytes);
		sessions.put(token,
				new Session(user.getId(), clock.now().plus(lifetime)));
		return token;
	}

	public Optional<User> findUser(String token) {
		if (token == null) {
			return Optional.empty();
		}

		Session session = sessions.get(token);
		if (session == null) {
			return Optional.empty();
		}

		if (!clock.now().isBefore(session.expiresAt())) {
			sessions.remove(token);
			return Optional.empty();
		}

		// still valid: renew it, so active players stay logged in
		sessions.put(token,
				new Session(session.userId(), clock.now().plus(lifetime)));

		return userRepository.getById(session.userId());
	}

	// token stops working, log out
	public void endSession(String token) {
		if (token != null) {
			sessions.remove(token);
		}
	}

	public static String tokenFromHeader(String authorization) {
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return null;
		}
		return authorization.substring("Bearer ".length());
	}
}
