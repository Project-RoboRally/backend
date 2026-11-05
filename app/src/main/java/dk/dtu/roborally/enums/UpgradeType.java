package dk.dtu.roborally.enums;

import lombok.Getter;

/**
 * The upgrades that can be bought in the shop. Costs are the ones printed on
 * the cards in the rulebook.
 *
 * @author Sebastian
 */
@Getter
public enum UpgradeType {
	ENERGY_ROUTINE(3, CommandInstruction.ENERGY_ROUTINE), // Extra Power Up
	SPEED_ROUTINE(3, CommandInstruction.SPEED_ROUTINE), // Extra Move 3
	REPEAT_ROUTINE(3, CommandInstruction.REPEAT_ROUTINE), // Extra Again
	SANDBOX_ROUTINE(5, CommandInstruction.SANDBOX_ROUTINE), // Any move or turn
	WEASEL_ROUTINE(3, CommandInstruction.WEASEL_ROUTINE), // Any turn
	SPAM_FOLDER(2, CommandInstruction.SPAM_FOLDER); // Gets rid of a SPAM

	private final int cost;
	private final CommandInstruction routine;

	UpgradeType(int cost, CommandInstruction routine) {
		this.cost = cost;
		this.routine = routine;
	}
}
