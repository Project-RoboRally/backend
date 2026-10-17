package dk.dtu.roborally.api.dto;

import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.lobby.GameLobby;

import java.util.List;

/**
 * Represents lobby information and its players. {@code gameId} is null until
 * the owner has started the game; once set, clients should navigate to that
 * game.
 *
 * @author Nicoleta, August
 */

// TODO: update once we've talked about boards creation
public record LobbyDTO(String id, String name, List<String> users,
		String createdBy, List<LobbyMemberDTO> members, String gameId) {

	public static LobbyDTO from(GameLobby lobby) {
		List<String> usernames = lobby.getUsersInLobby().stream()
				.map(User::getUsername).toList();
		String createdBy = lobby.getLobbyOwner() == null
				? null
				: lobby.getLobbyOwner().getUsername();
		String ownerId = lobby.getLobbyOwner() == null
				? null
				: lobby.getLobbyOwner().getId();
		List<LobbyMemberDTO> members = lobby.getMembers().stream()
				.map(member -> LobbyMemberDTO.from(member, ownerId)).toList();
		return new LobbyDTO(lobby.getLobbyId(), lobby.getLobbyName(), usernames,
				createdBy, members, lobby.getGameId());
	}
}
