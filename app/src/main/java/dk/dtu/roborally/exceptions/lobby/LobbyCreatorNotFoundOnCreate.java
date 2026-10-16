package dk.dtu.roborally.exceptions.lobby;

public class LobbyCreatorNotFoundOnCreate extends RuntimeException {
	public LobbyCreatorNotFoundOnCreate(String lobbyName) {
		super("Creator of lobby not found : " + lobbyName);
	}
}
