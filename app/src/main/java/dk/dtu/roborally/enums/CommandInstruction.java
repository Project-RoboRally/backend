package dk.dtu.roborally.enums;

/**
 * Hardcoded enum for all possible instructions.
 *
 * @author Matthias
 */

public enum CommandInstruction {
	MOVE_1, MOVE_2, MOVE_3, LEFT_TURN, RIGHT_TURN, U_TURN, MOVE_BACK, AGAIN;

	/**
	 * Spaces a forward move travels, one step at a time. Other instructions
	 * have no step count.
	 */
	public int steps() {
		return switch (this) {
			case MOVE_1 -> 1;
			case MOVE_2 -> 2;
			case MOVE_3 -> 3;
			default ->
				throw new IllegalArgumentException(this + " has no step count");
		};
	}
}
