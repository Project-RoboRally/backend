package dk.dtu.roborally.models.game;

/**
 * Whether a player's client is still sending health pings. {@code blocking} is
 * true when the player is offline and the game is waiting for them.
 *
 * @author Elias
 */
public record PlayerPresence(String username, boolean online,
		boolean blocking) {
}
