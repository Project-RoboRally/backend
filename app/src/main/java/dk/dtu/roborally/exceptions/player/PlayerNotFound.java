package dk.dtu.roborally.exceptions.player;

public class PlayerNotFound extends RuntimeException {
	public PlayerNotFound() {
		super("Player not found.");
	}
}
