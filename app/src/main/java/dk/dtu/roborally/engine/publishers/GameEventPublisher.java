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

	/**
	 * Sends every player in the game the full game state, including their own
	 */
	void publishGameStarted(String lobbyId, Game game);

	/**
	 * Sends the board layout, including robots placed on it, to everyone
	 * subscribed to the lobby.
	 */
	void publishBoard(String lobbyId, Game game);
}
