package dk.dtu.roborally.models.cards;

import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.DamageInstruction;

/**
 * Represents a programmable card programming cards and damage cards will be sub
 * types of this
 *
 * @author August, Sebastian
 */

public abstract class ProgrammableCard extends Card {

	/** The instruction name, like MOVE_1 or SPAM. Unique across both kinds. */
	public abstract String getName();

	/** Makes a card from a name given by {@link #getName()}. */
	public static ProgrammableCard named(String name) {
		for (CommandInstruction instruction : CommandInstruction.values()) {
			if (instruction.name().equals(name)) {
				return new ProgrammingCard(instruction);
			}
		}
		for (DamageInstruction instruction : DamageInstruction.values()) {
			if (instruction.name().equals(name)) {
				return new DamageCard(instruction);
			}
		}
		throw new IllegalArgumentException("Unknown card: " + name);
	}
}
