package dk.dtu.roborally.exceptions.lobby;

public class LobbyInvalidName extends RuntimeException {
	public LobbyInvalidName(String msg) {
		super("Invalid name for lobby: " + msg);
	}
}
