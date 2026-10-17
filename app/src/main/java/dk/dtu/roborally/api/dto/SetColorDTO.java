package dk.dtu.roborally.api.dto;

import dk.dtu.roborally.enums.PlayerColor;

/**
 * Request body for choosing a robot color in a lobby.
 *
 * @author August
 */
public record SetColorDTO(String username, PlayerColor color) {
}
