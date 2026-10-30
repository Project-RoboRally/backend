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
	ENERGY_ROUTINE(3, CommandInstruction.ENERGY_ROUTINE), SPEED_ROUTINE(3,
			CommandInstruction.SPEED_ROUTINE), REPEAT_ROUTINE(3,
					CommandInstruction.REPEAT_ROUTINE);

	private final int cost;
	private final CommandInstruction routine;

	UpgradeType(int cost, CommandInstruction routine) {
		this.cost = cost;
		this.routine = routine;
	}
}
