package dk.dtu.roborally.repository;

import dk.dtu.roborally.models.game.Game;

import java.util.List;
import java.util.Optional;

/**
 * Basic interface for games repository
 *
 * @author Elias
 */
public interface GameRepository {
	boolean add(Game game);

	boolean exists(String gameID);

	Optional<Game> getById(String gameID);

	List<Game> getAll();

	void delete(String gameID);

	/**
	 * Persists the current state of a game that is already stored. Called after
	 * each change so a restart can resume the game. In-memory storage keeps the
	 * same instance, so the default does nothing.
	 */
	default void update(Game game) {
	}
}
