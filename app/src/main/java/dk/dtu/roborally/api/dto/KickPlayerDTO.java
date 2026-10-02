package dk.dtu.roborally.api.dto;

/**
 * Contains the data required to kick a player out of a lobby.
 *
 * @author Nicoleta
 */
public record KickPlayerDTO(String kickedBy, String playerKicked) {
}
