package dk.dtu.roborally.exceptions.game;

/**
 * Thrown when a game is requested that does not exist.
 *
 * @author Nicoleta
 */
public class GameNotFoundException extends RuntimeException {
	public GameNotFoundException(String id) {
		super("Game not found: " + id);
	}
}
