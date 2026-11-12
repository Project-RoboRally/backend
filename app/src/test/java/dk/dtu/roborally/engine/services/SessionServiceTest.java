package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.helpers.TestClock;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {
	private SessionService sessionService;
	private User alice;

	@BeforeEach
	void setUp() {
		InMemoryUserRepository userRepository = new InMemoryUserRepository();
		alice = new User("alice");
		userRepository.add(alice);
		sessionService = new SessionService(userRepository);
	}

	@Test
	void aTokenLeadsBackToItsUser() {
		String token = sessionService.createSession(alice);

		assertSame(alice, sessionService.findUser(token).orElseThrow());
	}

	@Test
	void everyLoginGetsADifferentToken() {
		assertNotEquals(sessionService.createSession(alice),
				sessionService.createSession(alice));
	}

	@Test
	void unknownTokensBelongToNobody() {
		assertTrue(sessionService.findUser("some-token").isEmpty());
		assertTrue(sessionService.findUser(null).isEmpty());
	}

	@Test
	void aTokenStopsWorkingAfterLogout() {
		String token = sessionService.createSession(alice);

		sessionService.endSession(token);

		assertTrue(sessionService.findUser(token).isEmpty());
	}

	@Test
	void anUnusedTokenStopsWorkingAfterItsLifetime() {
		InMemoryUserRepository userRepository = new InMemoryUserRepository();
		userRepository.add(alice);
		TestClock clock = new TestClock();
		SessionService expiring = new SessionService(userRepository, clock,
				Duration.ofHours(24));
		String token = expiring.createSession(alice);

		clock.advance(Duration.ofHours(24));

		assertTrue(expiring.findUser(token).isEmpty());
	}

	@Test
	void aTokenThatKeepsBeingUsedDoesNotExpire() {
		InMemoryUserRepository userRepository = new InMemoryUserRepository();
		userRepository.add(alice);
		TestClock clock = new TestClock();
		SessionService expiring = new SessionService(userRepository, clock,
				Duration.ofHours(24));
		String token = expiring.createSession(alice);

		// used every 20 hours: 60 hours in total, but never 24 hours unused
		for (int i = 0; i < 3; i++) {
			clock.advance(Duration.ofHours(20));
			assertTrue(expiring.findUser(token).isPresent());
		}
	}

	@Test
	void theTokenIsReadFromTheAuthorizationHeader() {
		assertEquals("abc123", SessionService.tokenFromHeader("Bearer abc123"));
		assertNull(SessionService.tokenFromHeader("abc123"));
		assertNull(SessionService.tokenFromHeader(null));
	}
}
