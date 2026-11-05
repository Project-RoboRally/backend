package dk.dtu.roborally.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Restricts player-private state subscriptions to the authenticated player.
 *
 * @author Anton
 */
public class PlayerStateSubscriptionInterceptor implements ChannelInterceptor {

	private static final Pattern PLAYER_STATE_DESTINATION = Pattern
			.compile("^/game/lobby/[^/]+/player/([^/]+)$");

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor
				.getAccessor(message, StompHeaderAccessor.class);
		if (accessor != null
				&& StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
			String destination = accessor.getDestination();
			Matcher matcher = destination == null
					? null
					: PLAYER_STATE_DESTINATION.matcher(destination);
			if (matcher != null && matcher.matches()
					&& (accessor.getUser() == null || !accessor.getUser()
							.getName().equals(matcher.group(1)))) {
				throw new AccessDeniedException(
						"Players may only subscribe to their own game state");
			}
		}
		return message;
	}
}
