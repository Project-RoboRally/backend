package dk.dtu.roborally.exceptions.game;

/**
 * Thrown when a game cannot be found by its ID.
 *
 * @author Matthias
 */
public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(String gameId) {
        super("Game not found: " + gameId);
    }
}