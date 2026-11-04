package dk.dtu.roborally.engine.publishers;

import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.RoundResult;

/**
 * Publishes game events to connected clients. Keeps the engine independent of
 * the transport used (STOMP over WebSocket).
 *
 * @author Nicoleta, Sebastian
 */
public interface GameEventPublisher {
	void publishStatus(Game game);

	/**
	 * Sends every player in the game the full game state, including their own
	 */
	void publishGameStarted(String lobbyId, Game game);

	/**
	 * Sends every player the full game state when a new round starts with its
	 * upgrade phase.
	 */
	void publishRoundStarted(String lobbyId, Game game);

	/**
	 * Sends every player the full game state after someone bought or skipped.
	 */
	void publishUpgradeTurn(String lobbyId, Game game);

	/**
	 * Sends every player the full game state when programming starts, so they
	 * get their new hand.
	 */
	void publishProgrammingStarted(String lobbyId, Game game);

	/**
	 * Sends the board layout, including robots placed on it, to everyone
	 * subscribed to the lobby.
	 */
	void publishBoard(String lobbyId, Game game);

	/**
	 * Same board message, plus one pose per robot after each turn or step and
	 * the cards played in each register, so clients can animate the round. The
	 * default drops both.
	 */
	default void publishRound(String lobbyId, Game game, RoundResult result) {
		publishBoard(lobbyId, game);
	}
}
