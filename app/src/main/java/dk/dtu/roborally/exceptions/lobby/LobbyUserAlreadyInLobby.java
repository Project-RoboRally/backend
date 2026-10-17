package dk.dtu.roborally.exceptions.lobby;

public class LobbyUserAlreadyInLobby extends RuntimeException {
	public LobbyUserAlreadyInLobby(String username) {
		super("User " + username + " is already in a lobby.");
	}
}
