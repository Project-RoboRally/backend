package dk.dtu.roborally.api.dto;

/**
 * Request body for toggling a user's ready state in a lobby.
 *
 * @author August
 */
public record SetReadyDTO(String username, boolean ready) {
}
