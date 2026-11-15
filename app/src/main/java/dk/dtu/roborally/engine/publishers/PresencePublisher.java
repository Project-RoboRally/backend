package dk.dtu.roborally.engine.publishers;

import dk.dtu.roborally.models.game.PlayerPresence;

import java.util.List;

/**
 * Publishes which players in a game are online.
 *
 * @author Elias
 */
public interface PresencePublisher {
	void publishPresence(String gameId, List<PlayerPresence> players);
}
