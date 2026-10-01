package dk.dtu.roborally.exceptions.lobby;

public class LobbyPlayerAlreadyInLobby extends RuntimeException {
	public LobbyPlayerAlreadyInLobby(String username) {
		super("User " + username + " is already in a lobby.");
	}
}
