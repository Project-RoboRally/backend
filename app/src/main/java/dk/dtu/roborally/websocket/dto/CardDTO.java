package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.models.cards.DamageCard;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;

/**
 * A programming or damage card. {@code instruction} is the name of the matching
 * CommandInstruction or DamageInstruction.
 *
 * @author Elias
 */
public record CardDTO(String type, String instruction) {

	public static CardDTO from(ProgrammableCard card) {
		// Convert the card to the corresponding DTO
		return switch (card) {
			case ProgrammingCard programming ->
				new CardDTO("PROGRAMMING", programming.getInstruction().name());
			case DamageCard damage ->
				new CardDTO("DAMAGE", damage.getInstruction().name());
			default -> throw new IllegalArgumentException(
					"Unknown card: " + card.getClass().getName());
		};
	}
}
