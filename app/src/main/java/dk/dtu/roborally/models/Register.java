package dk.dtu.roborally.models;

import dk.dtu.roborally.models.Cards.ProgrammableCard;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents one programming register on a player's mat.
 *
 * @author Matthias
 */
public class Register {

	@Getter
	private final int index;

	@Getter
	@Setter
	private ProgrammableCard card;

	public Register(int index) {
		this.index = index;
	}
}
