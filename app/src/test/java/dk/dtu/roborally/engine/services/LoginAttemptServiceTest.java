package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.helpers.TestClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {
	private TestClock clock;
	private LoginAttemptService attempts;

	@BeforeEach
	void setUp() {
		clock = new TestClock();
		attempts = new LoginAttemptService(clock, 3, Duration.ofMinutes(15));
	}

	@Test
	void aFewFailuresDoNotBlock() {
		attempts.recordFailure("alice");
		attempts.recordFailure("alice");

		assertFalse(attempts.isBlocked("alice"));
	}

	@Test
	void tooManyFailuresBlockTheUsername() {
		attempts.recordFailure("alice");
		attempts.recordFailure("alice");
		attempts.recordFailure("alice");

		assertTrue(attempts.isBlocked("alice"));
		assertFalse(attempts.isBlocked("bob"));
	}

	@Test
	void theBlockEndsAfterTheLockTime() {
		attempts.recordFailure("alice");
		attempts.recordFailure("alice");
		attempts.recordFailure("alice");

		clock.advance(Duration.ofMinutes(15));

		assertFalse(attempts.isBlocked("alice"));
	}

	@Test
	void aSuccessfulLoginResetsTheCount() {
		attempts.recordFailure("alice");
		attempts.recordFailure("alice");
		attempts.recordSuccess("alice");
		attempts.recordFailure("alice");

		assertFalse(attempts.isBlocked("alice"));
	}
}
