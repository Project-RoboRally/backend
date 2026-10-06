package dk.dtu.roborally.models.player;

import dk.dtu.roborally.models.cards.ProgrammableCard;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents one programming register on a player's mat.
 *
 * @author Matthias
 */
public class Register {

	@Getter
	private final int registerNumber;

	@Getter
	@Setter
	private ProgrammableCard card;

	public Register(int registerNumber) {
		if (registerNumber < 1 || registerNumber > 5) {
			throw new IllegalArgumentException(
					"Register number must be between 1 and 5.");
		}

		this.registerNumber = registerNumber;
	}
}