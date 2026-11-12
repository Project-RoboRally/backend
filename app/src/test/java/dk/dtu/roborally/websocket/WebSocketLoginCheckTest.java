package dk.dtu.roborally.websocket;

import dk.dtu.roborally.engine.services.SessionService;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketLoginCheckTest {
	private SessionService sessionService;
	private WebSocketLoginCheck interceptor;
	private String aliceToken;

	@BeforeEach
	void setUp() {
		InMemoryUserRepository userRepository = new InMemoryUserRepository();
		User alice = new User("alice");
		userRepository.add(alice);
		sessionService = new SessionService(userRepository);
		interceptor = new WebSocketLoginCheck(sessionService);
		aliceToken = sessionService.createSession(alice);
	}

	private Message<byte[]> connect(String authorization) {
		StompHeaderAccessor accessor = StompHeaderAccessor
				.create(StompCommand.CONNECT);
		if (authorization != null) {
			accessor.setNativeHeader("Authorization", authorization);
		}
		accessor.setLeaveMutable(true);
		return MessageBuilder.createMessage(new byte[0],
				accessor.getMessageHeaders());
	}

	private Message<byte[]> subscribe(String destination, Principal user) {
		StompHeaderAccessor accessor = StompHeaderAccessor
				.create(StompCommand.SUBSCRIBE);
		accessor.setDestination(destination);
		accessor.setUser(user);
		accessor.setLeaveMutable(true);
		return MessageBuilder.createMessage(new byte[0],
				accessor.getMessageHeaders());
	}

	@Test
	void connectingWithAValidTokenSetsTheUser() {
		Message<?> result = interceptor.preSend(connect("Bearer " + aliceToken),
				null);

		Principal user = StompHeaderAccessor.wrap(result).getUser();
		assertEquals("alice", user.getName());
	}

	@Test
	void connectingWithoutAValidTokenIsRejected() {
		assertThrows(MessagingException.class,
				() -> interceptor.preSend(connect(null), null));
		assertThrows(MessagingException.class,
				() -> interceptor.preSend(connect("Bearer made-up"), null));
	}

	@Test
	void aPlayerCanSubscribeToTheirOwnChannel() {
		assertDoesNotThrow(() -> interceptor.preSend(
				subscribe("/game/lobby/l1/player/alice", () -> "alice"), null));
		assertDoesNotThrow(() -> interceptor.preSend(
				subscribe("/app/lobby/l1/player/alice", () -> "alice"), null));
	}

	@Test
	void aPlayerCannotSubscribeToSomeoneElsesChannel() {
		assertThrows(MessagingException.class,
				() -> interceptor.preSend(
						subscribe("/game/lobby/l1/player/bob", () -> "alice"),
						null));
	}

	@Test
	void sharedChannelsAreStillOpen() {
		assertDoesNotThrow(() -> interceptor.preSend(
				subscribe("/game/lobby/l1/board", () -> "alice"), null));
	}
}
