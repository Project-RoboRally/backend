package dk.dtu.roborally.models;

import java.util.ArrayList;
import java.util.List;

import dk.dtu.roborally.models.Cards.CardDeck;
import dk.dtu.roborally.models.Cards.ProgrammableCard;
import lombok.Getter;

/**
 * Represents the individual player mat used to store programming registers and
 * player-specific robot state.
 *
 * @author Matthias, August
 */
public class PlayerMat {

	private static final int REGISTER_COUNT = 5;

	private final CardDeck<ProgrammableCard> drawPile = new CardDeck<>();
	private final List<ProgrammableCard> hand = new ArrayList<>();
	private final List<ProgrammableCard> discardPile = new ArrayList<>();
	private final Register[] registers = new Register[REGISTER_COUNT];
	@Getter
	private boolean submitted;

	public PlayerMat() {
		for (int i = 0; i < registers.length; i++) {
			registers[i] = new Register(i);
		}
	}
	// TODO: Implement logic for drawing cards, submitting registers, and discarding cards.

	public List<ProgrammableCard> getHand() {
		return List.copyOf(hand);
	}

	public List<ProgrammableCard> getDiscardPile() {
    	return List.copyOf(discardPile);
	}

	public int getDrawPileSize() {
    	return drawPile.size();
	}
}
