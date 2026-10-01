package dk.dtu.roborally.api.dto;

/**
 * Contains the data required to kick a user out of a lobby.
 *
 * @author Nicoleta
 */
public record KickUserDTO(String kickedBy, String userKicked) {
}
