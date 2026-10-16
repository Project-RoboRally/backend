package dk.dtu.roborally.api.dto;

import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.models.lobby.LobbyMember;

/**
 * Represents one lobby member with their assigned color and ready state.
 *
 * @author August
 */
public record LobbyMemberDTO(String username, PlayerColor color, boolean ready,
		boolean owner) { 

	public static LobbyMemberDTO from(LobbyMember member, String ownerId) {
		return new LobbyMemberDTO(member.user().getUsername(), member.color(),
				member.ready(), member.user().getId().equals(ownerId));
	}
}
