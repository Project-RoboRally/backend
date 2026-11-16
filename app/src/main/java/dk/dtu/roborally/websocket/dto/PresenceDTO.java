package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.models.game.KickVote;
import dk.dtu.roborally.models.game.PlayerPresence;

import java.util.List;

/**
 * WebSocket payload saying which players in a game are online, and any kick
 * vote in progress.
 *
 * @author Elias
 */
public record PresenceDTO(List<PlayerPresence> players, List<String> ask,
		KickVote vote) {
	public PresenceDTO(List<PlayerPresence> players) {
		this(players, List.of(), null);
	}
}
