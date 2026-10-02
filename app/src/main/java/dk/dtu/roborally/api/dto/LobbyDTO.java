package dk.dtu.roborally.api.dto;

import dk.dtu.roborally.models.GameLobby;
import dk.dtu.roborally.models.User;

import java.util.List;

/**
 * Represents lobby information and its players.
 *
 * @author Nicoleta
 */

// TODO: update once we've talked about boards creation
public record LobbyDTO(String id, String name, List<String> users,
		String createdBy) {

	public static LobbyDTO from(GameLobby lobby) {
		List<String> usernames = lobby.getUsersInLobby().stream()
				.map(User::getUsername).toList();
		String createdBy = lobby.getLobbyOwner() == null
				? null
				: lobby.getLobbyOwner().getUsername();
		return new LobbyDTO(lobby.getLobbyId(), lobby.getLobbyName(), usernames,
				createdBy);
	}
}
