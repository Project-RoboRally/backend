package dk.dtu.roborally.models.player;

import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.models.cards.CardDeck;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Represents the individual player mat used to store programming registers and
 * player-specific robot state.
 *
 * @author Matthias, August
 */
public class PlayerMat {

	private static final int REGISTER_COUNT = 5;

	/**
	 * Instructions a player may program. This is the only allow-list: a new
	 * card is added here and handled when a register activates.
	 */
	public static final Set<CommandInstruction> ALLOWED = Set.of(
			CommandInstruction.MOVE_1, CommandInstruction.MOVE_2, CommandInstruction.MOVE_3,
			CommandInstruction.LEFT_TURN, CommandInstruction.RIGHT_TURN);

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
	// TODO: Implement logic for drawing cards and discarding cards.

	/**
	 * Fills the five registers and marks the mat submitted. A second program is
	 * rejected until {@link #clearProgram()} runs.
	 */
	public void program(List<CommandInstruction> instructions) {
		if (submitted) {
			throw new IllegalStateException(
					"Program has already been submitted");
		}
		if (instructions == null || instructions.size() != REGISTER_COUNT) {
			throw new IllegalArgumentException("A program must contain exactly "
					+ REGISTER_COUNT + " instructions");
		}
		for (CommandInstruction instruction : instructions) {
			if (!ALLOWED.contains(instruction)) {
				throw new IllegalArgumentException(
						"Instruction is not allowed: " + instruction);
			}
		}
		for (int i = 0; i < REGISTER_COUNT; i++) {
			registers[i].setCard(new ProgrammingCard(instructions.get(i)));
		}
		submitted = true;
	}

	/** Empties the registers so the next round can be programmed. */
	public void clearProgram() {
		for (Register register : registers) {
			register.setCard(null);
		}
		submitted = false;
	}

	public List<ProgrammableCard> getHand() {
		return Collections.unmodifiableList(hand);
	}

	public List<ProgrammableCard> getDiscardPile() {
		return Collections.unmodifiableList(discardPile);
	}

	public List<Register> getRegisters() {
		return List.of(registers);
	}

	public int getDrawPileSize() {
		return drawPile.size();
	}
}
