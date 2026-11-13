package dk.dtu.roborally.engine.publishers;

import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.RoundResult;

/**
 * Publishes game events to connected clients. Keeps the engine independent of
 * the transport used (STOMP over WebSocket).
 *
 * @author Nicoleta, Sebastian, Elias
 */
public interface GameEventPublisher {
	void publishStatus(Game game);

	/**
	 * Sends every player in the game the full game state, including their own
	 */
	void publishGameStarted(Game game);

	/**
	 * Sends every player the full game state when a new round starts with its
	 * upgrade phase.
	 */
	void publishRoundStarted(Game game);

	/**
	 * Sends every player the full game state after someone bought or skipped.
	 */
	void publishUpgradeTurn(Game game);

	/**
	 * Sends every player the full game state when programming starts, so they
	 * get their new hand.
	 */
	void publishProgrammingStarted(Game game);

	/**
	 * Sends every player the full game state after someone used an upgrade
	 * while programming, so the player sees their changed hand.
	 */
	void publishUpgradeUsed(Game game);

	/**
	 * Sends every player the full game state when the first program is in and
	 * the programming timer starts. The state says when it runs out.
	 */
	void publishTimerStarted(Game game);

	/**
	 * Sends every player the full game state when activation stops to wait for
	 * a player's choice. The state says who has to choose and between what.
	 */
	void publishChoiceRequired(Game game);

	/**
	 * Sends the board layout, including robots placed on it, to everyone
	 * subscribed to the game.
	 */
	void publishBoard(Game game);

	/**
	 * Same board message, plus one pose per robot after each turn or step and
	 * the cards played in each register, so clients can animate the round. The
	 * default drops both.
	 */
	default void publishRound(Game game, RoundResult result) {
		publishBoard(game);
	}
}
