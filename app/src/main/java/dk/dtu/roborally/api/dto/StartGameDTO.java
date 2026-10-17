package dk.dtu.roborally.api.dto;

/**
 * Request body for starting the game from a lobby. Only the lobby owner is
 * allowed to start.
 *
 * @author August
 */
public record StartGameDTO(String username) {
}
