package dk.dtu.roborally.exceptions.lobby;

public class UserNotInLobby extends RuntimeException {
	public UserNotInLobby(String username) {
		super("User " + username + " is not in a lobby.");
	}
}
