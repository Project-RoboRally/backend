package dk.dtu.roborally.websocket;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;

import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.engine.services.LobbyService;

/**
 * Verifies private game state cannot be requested for another account.
 */
class PlayerStateAuthorizationTest {

	@Test
	void rejectsRequestsForAnotherPlayersPrivateState() {
		PlayerStateController controller = new PlayerStateController(
				mock(LobbyService.class), mock(GameService.class));

		assertThrows(AccessDeniedException.class,
				() -> controller.playerState("lobby-id", "alice", () -> "bob"));
	}

	@Test
	void rejectsSubscriptionToAnotherPlayersPrivateTopic() {
		PlayerStateSubscriptionInterceptor interceptor = new PlayerStateSubscriptionInterceptor();
		Message<?> message = subscription("/game/lobby/lobby-id/player/alice",
				() -> "bob");

		assertThrows(AccessDeniedException.class, () -> interceptor.preSend(
				message,
				mock(org.springframework.messaging.MessageChannel.class)));
	}

	@Test
	void allowsSubscriptionToOwnPrivateTopic() {
		PlayerStateSubscriptionInterceptor interceptor = new PlayerStateSubscriptionInterceptor();
		Message<?> message = subscription("/game/lobby/lobby-id/player/alice",
				() -> "alice");

		assertNotNull(interceptor.preSend(message,
				mock(org.springframework.messaging.MessageChannel.class)));
	}

	private Message<?> subscription(String destination, Principal principal) {
		StompHeaderAccessor accessor = StompHeaderAccessor
				.create(StompCommand.SUBSCRIBE);
		accessor.setDestination(destination);
		accessor.setUser(principal);
		return MessageBuilder.createMessage("", accessor.getMessageHeaders());
	}
}
