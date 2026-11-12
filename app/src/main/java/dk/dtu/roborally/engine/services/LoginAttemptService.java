package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.engine.GameClock;
import dk.dtu.roborally.engine.SystemGameClock;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Counts failed logins per username. After too many wrong passwords in a row,
 * that username is locked for a while.
 *
 * @author Nicoleta
 */
public class LoginAttemptService {
	private static final int DEFAULT_MAX_FAILURES = 5;
	private static final Duration DEFAULT_LOCK_TIME = Duration.ofMinutes(15);

	private final GameClock clock;
	private final int maxFailures;
	private final Duration lockTime;
	private final Map<String, Integer> failures = new HashMap<>();
	private final Map<String, Instant> lockedUntil = new HashMap<>();

	public LoginAttemptService() {
		this(new SystemGameClock(), DEFAULT_MAX_FAILURES, DEFAULT_LOCK_TIME);
	}

	public LoginAttemptService(GameClock clock, int maxFailures,
			Duration lockTime) {
		this.clock = clock;
		this.maxFailures = maxFailures;
		this.lockTime = lockTime;
	}

	public synchronized boolean isBlocked(String username) {
		Instant until = lockedUntil.get(username);
		return until != null && clock.now().isBefore(until);
	}

	public synchronized void recordFailure(String username) {
		int count = failures.getOrDefault(username, 0) + 1;
		if (count >= maxFailures) {
			lockedUntil.put(username, clock.now().plus(lockTime));
			failures.remove(username);
		} else {
			failures.put(username, count);
		}
	}

	public synchronized void recordSuccess(String username) {
		failures.remove(username);
		lockedUntil.remove(username);
	}
}
