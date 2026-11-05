package dk.dtu.roborally.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP-over-WebSocket setup for the real-time game API.
 * <p>
 * Clients connect to {@code /ws}, send actions to destinations prefixed with
 * {@code /app} (routed to {@code @MessageMapping} controllers), and subscribe
 * to {@code /game/...} destinations to receive broadcasts.
 *
 * @author Elias, Nicoleta, Anton
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Value("${app.cors.allowed-origin-patterns}")
	private String[] allowedOriginPatterns;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws").setAllowedOriginPatterns(allowedOriginPatterns)
				.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/game"); // Enable a simple message broker
												// for the game
		registry.setApplicationDestinationPrefixes("/app"); // Set the
															// application
															// destination
															// prefix to /app
	}

	@Override
	public void configureClientInboundChannel(
			ChannelRegistration registration) {
		registration.interceptors(new PlayerStateSubscriptionInterceptor());
	}
}
