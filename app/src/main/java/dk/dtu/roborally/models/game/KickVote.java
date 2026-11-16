package dk.dtu.roborally.models.game;

import java.util.List;

/**
 * A vote to kick an offline player. {@code needed} is every player who is
 * online now, except the target; the kick happens when they have all voted yes.
 *
 * @author Elias
 */
public record KickVote(String target, List<String> needed, List<String> yes) {
}
