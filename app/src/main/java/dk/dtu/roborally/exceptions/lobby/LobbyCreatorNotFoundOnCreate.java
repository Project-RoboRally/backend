package dk.dtu.roborally.exceptions.lobby;
/**
 * @author Elias
 */

public class LobbyCreatorNotFoundOnCreate extends RuntimeException {
	public LobbyCreatorNotFoundOnCreate(String lobbyName) {
		super("Creator of lobby not found : " + lobbyName);
	}
}
