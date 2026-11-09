package dk.dtu.roborally.api.dto;

/**
 * Contains the data required to kick a user out of a lobby so they cannot join,
 * and to unkick a user so they can join again.
 *
 * @author Nicoleta
 */
public record KickUserDTO(String kickedBy, String userKicked) {
}
