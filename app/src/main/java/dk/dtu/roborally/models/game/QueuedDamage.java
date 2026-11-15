package dk.dtu.roborally.models.game;

/**
 * A damage card that could not be taken because its pile was empty. The player
 * picks another type, then the card goes to their discard pile or the top of
 * their deck.
 *
 * @author Elias
 */
public record QueuedDamage(String playerId, boolean onTopOfDeck) {
}
