package dk.dtu.roborally.engine.publishers;

import dk.dtu.roborally.models.game.Game;

/**
 * Publishes game events to connected clients. Keeps the engine independent of
 * the transport used (STOMP over WebSocket).
 *
 * @author Nicoleta
 */
public interface GameEventPublisher {
	void publishStatus(Game game);
}

