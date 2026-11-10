package dk.dtu.roborally.enums;

/**
 * Hardcoded enum for all possible damage instructions, with how many of each
 * card the game comes with.
 *
 * @author August, Sebastian
 */
public enum DamageInstruction {
	SPAM(38), TROJAN_HORSE(12), WORM(6), VIRUS(18);

	private final int copies;

	DamageInstruction(int copies) {
		this.copies = copies;
	}

	public int getCopies() {
		return copies;
	}
}
