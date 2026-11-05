package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
