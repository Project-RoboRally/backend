package dk.dtu.roborally.exceptions.lobby;

/**
 * Thrown when a lobby is requested that does not exist.
 *
 * @author Nicoleta
 */
public class LobbyNotFoundException extends RuntimeException {
    public LobbyNotFoundException(String id) {
        super("Lobby not found: " + id);
    }
}
