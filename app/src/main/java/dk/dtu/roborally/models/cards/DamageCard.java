package dk.dtu.roborally.models.cards;

import java.util.Objects;

import dk.dtu.roborally.enums.DamageInstruction;
import lombok.Getter;

/**
 * Represents a damage card in the game, this holds a penalty instruction, for
 * when a players robot takes damage in some way, that the player can use to
 * make up their program
 *
 * @author August
 */

@Getter
public class DamageCard extends ProgrammableCard {
	private final DamageInstruction instruction;

	public DamageCard(DamageInstruction instruction) {
		this.instruction = Objects.requireNonNull(instruction);
	}
}
