package dk.dtu.roborally.api.dto;

/**
 * Contains the data required to create a lobby.
 *
 * @author Nicoleta
 */
public record CreateLobbyDTO(String id, String name, String username) {
}
