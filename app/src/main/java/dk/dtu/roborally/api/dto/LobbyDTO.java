package dk.dtu.roborally.api.dto;

import java.util.List;

/**
 * Represents lobby information and its players.
 *
 * @author Nicoleta
 */

// TODO: update once Lobby model is updated with id and name, and we've talked about boards creation
public record LobbyDTO(String id, String name, List<String> players, String createdBy) {
}
