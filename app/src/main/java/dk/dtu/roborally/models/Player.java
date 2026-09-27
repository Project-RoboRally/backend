package dk.dtu.roborally.models;

import java.util.Objects;

import dk.dtu.roborally.enums.PlayerColor;
import lombok.Getter;

/**
 * Represents a user's participation in a specific Robo Rally game. Holds the
 * user's game-specific robot, player mat and progress.
 *
 * @author Elias, Matthias, August
 */
@Getter
public class Player {
	private final String id;
	private final User user;
	private final PlayerColor color;
	private final PlayerMat playerMat;
	private Robot robot;
	private int checkpointsReached;

	public Player(String id, User user, PlayerColor color) {
		this.id = Objects.requireNonNull(id);
		this.user = Objects.requireNonNull(user);
		this.color = Objects.requireNonNull(color);
		this.playerMat = new PlayerMat();
	}
}
