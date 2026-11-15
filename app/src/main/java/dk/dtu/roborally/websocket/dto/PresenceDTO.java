package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.models.game.PlayerPresence;

import java.util.List;

/**
 * WebSocket payload saying which players in a game are online.
 *
 * @author Elias
 */
public record PresenceDTO(List<PlayerPresence> players) {
}
