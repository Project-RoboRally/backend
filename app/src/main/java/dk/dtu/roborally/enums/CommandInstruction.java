package dk.dtu.roborally.enums;

import java.util.List;

/**
 * Hardcoded enum for all possible instructions.
 *
 * @author Matthias, Sebastian
 */

public enum CommandInstruction {
	MOVE_1, MOVE_2, MOVE_3, LEFT_TURN, RIGHT_TURN, U_TURN, MOVE_BACK, POWER_UP, AGAIN, ENERGY_ROUTINE, SPEED_ROUTINE, REPEAT_ROUTINE, SANDBOX_ROUTINE, WEASEL_ROUTINE;

	/**
	 * Spaces a forward move travels, one step at a time. Other instructions
	 * have no step count.
	 */
	public int steps() {
		return switch (this) {
			case MOVE_1 -> 1;
			case MOVE_2 -> 2;
			case MOVE_3, SPEED_ROUTINE -> 3;
			default ->
				throw new IllegalArgumentException(this + " has no step count");
		};
	}

	/** Whether this repeats the previous register instead of acting itself. */
	public boolean repeatsPreviousRegister() {
		return this == AGAIN || this == REPEAT_ROUTINE;
	}

	/**
	 * The actions a player picks from when this card activates. Empty for cards
	 * that just do one thing.
	 */
	public List<CommandInstruction> actionsToChooseFrom() {
		return switch (this) {
			case SANDBOX_ROUTINE -> List.of(MOVE_1, MOVE_2, MOVE_3, MOVE_BACK,
					LEFT_TURN, RIGHT_TURN, U_TURN);
			case WEASEL_ROUTINE -> List.of(LEFT_TURN, RIGHT_TURN, U_TURN);
			default -> List.of();
		};
	}
}
