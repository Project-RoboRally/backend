package dk.dtu.roborally.models.player;

import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.models.User;
import lombok.Getter;

import java.util.Objects;

/**
 * Represents a user's participation in a specific Robo Rally game. Holds the
 * user's game-specific robot, player mat and progress.
 *
 * @author Elias, Matthias, August, Sebastian
 */
@Getter
public class Player {

	private static final int STARTING_ENERGY = 5;

	private final String id;
	private final User user;
	private final PlayerMat playerMat;
	private final Robot robot;
	private final PlayerColor color;
	private int checkpointsReached;
	private int energy = STARTING_ENERGY;

	public Player(String id, User user, PlayerColor color, Robot robot) {
		this.id = Objects.requireNonNull(id);
		this.user = Objects.requireNonNull(user);
		this.color = Objects.requireNonNull(color);
		this.robot = Objects.requireNonNull(robot);
		this.playerMat = new PlayerMat();
	}

	public void addEnergy(int amount) {
		energy += amount;
	}

	public void spendEnergy(int amount) {
		if (amount > energy) {
			throw new IllegalArgumentException("Not enough energy");
		}
		energy -= amount;
	}
}
