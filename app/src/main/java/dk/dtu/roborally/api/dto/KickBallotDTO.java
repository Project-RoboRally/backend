package dk.dtu.roborally.api.dto;

/**
 * A yes or no on an open kick vote. Every online player must vote yes.
 *
 * @author Elias
 */
public record KickBallotDTO(boolean yes) {
}
