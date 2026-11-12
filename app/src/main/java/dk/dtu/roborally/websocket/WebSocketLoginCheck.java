package dk.dtu.roborally.websocket;

import dk.dtu.roborally.engine.services.SessionService;
import dk.dtu.roborally.models.User;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;

/**
 * Checks every STOMP frame a client sends. CONNECT must carry a valid session
 * token, which decides who the connection belongs to. SUBSCRIBE to a player's
 * private channel is only allowed for that player.
 *
 * @Author Nicoleta
 */
public class WebSocketLoginCheck implements ChannelInterceptor {

	private final SessionService sessionService;

	public WebSocketLoginCheck(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor
				.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null) {
			return message;
		}

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			String token = SessionService.tokenFromHeader(
					accessor.getFirstNativeHeader("Authorization"));
			User user = sessionService.findUser(token)
					.orElseThrow(() -> new MessagingException("Not logged in"));
			String username = user.getUsername();
			Principal principal = () -> username;
			accessor.setUser(principal);
		}

		if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
			String destination = String.valueOf(accessor.getDestination());
			Principal user = accessor.getUser();
			if (destination.contains("/player/") && (user == null
					|| !destination.endsWith("/player/" + user.getName()))) {
				throw new MessagingException(
						"You can only subscribe to your own player channel");
			}
		}

		return message;
	}
}
