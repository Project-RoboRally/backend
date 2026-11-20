package dk.dtu.roborally.exceptions.lobby;
/**
 * @author Elias
 */

public class LobbyInvalidName extends RuntimeException {
	public LobbyInvalidName(String msg) {
		super("Invalid name for lobby: " + msg);
	}
}
