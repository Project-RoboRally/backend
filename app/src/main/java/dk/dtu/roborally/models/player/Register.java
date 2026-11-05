package dk.dtu.roborally.models.player;

import dk.dtu.roborally.models.cards.ProgrammableCard;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents one programming register on a player's mat.
 *
 * @author Matthias, Sebastian
 */
public class Register {

	@Getter
	private final int index;

	@Getter
	@Setter
	private ProgrammableCard card;

	/** Whether a damage card was played here and swapped for another card. */
	@Getter
	@Setter
	private boolean damaged;

	public Register(int index) {
		this.index = index;
	}
}
