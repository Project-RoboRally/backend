package dk.dtu.roborally.exceptions.lobby;

public class PlayerNotInLobby extends RuntimeException {
	public PlayerNotInLobby(String username) {
		super("User " + username + " is not in a lobby.");
	}
}
