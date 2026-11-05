package dk.dtu.roborally.models.player;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Vector2D;
import lombok.Getter;

/**
 * Represents a player's robot and its current state on the board.
 *
 * @author Elias, Matthias, August, Sebastian
 */
public class Robot {

	@Getter
	private Vector2D currentSpace;

	@Getter
	private Direction orientation;

	@Getter
	private boolean rebootedThisRound;

	/**
	 * Where the robot started the game, it reboots here from the start board.
	 */
	@Getter
	private final Vector2D startSpace;

	public Robot(Vector2D space, Direction orientation) {
		this(space, orientation, space);
	}

	public Robot(Vector2D space, Direction orientation, Vector2D startSpace) {
		this.currentSpace = space;
		this.orientation = orientation;
		this.startSpace = startSpace;
	}

	public void turnRight() {
		orientation = orientation.turnRight();
	}

	public void turnLeft() {
		orientation = orientation.turnLeft();
	}

	public void uTurn() {
		orientation = orientation.uTurn();
	}

	public void moveTo(Vector2D space) {
		currentSpace = space;
	}

	/** Puts the robot back on the board after falling off or into a pit. */
	public void reboot(Vector2D space) {
		currentSpace = space;
		rebootedThisRound = true;
	}

	/** A new activation phase starts, last round's reboot no longer counts. */
	public void clearReboot() {
		rebootedThisRound = false;
	}

	/** Used when a rebooted robot faces the way its player chose. */
	public void face(Direction direction) {
		orientation = direction;
	}

	/** Puts back the reboot flag loaded from storage. */
	public void restoreReboot(boolean rebooted) {
		rebootedThisRound = rebooted;
	}
}
