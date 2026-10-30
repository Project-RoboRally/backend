package dk.dtu.roborally.models.player;

import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.models.cards.CardDeck;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represents the individual player mat used to store programming registers and
 * player-specific robot state.
 *
 * @author Matthias, August, Sebastian
 */
public class PlayerMat {

	private static final int REGISTER_COUNT = 5;
	private static final int HAND_SIZE = 9;

	/**
	 * Instructions a player may program. This is the only allow-list: a new
	 * card is added here and handled when a register activates.
	 */
	public static final Set<CommandInstruction> ALLOWED = Set.of(
			CommandInstruction.MOVE_1, CommandInstruction.MOVE_2,
			CommandInstruction.MOVE_3, CommandInstruction.LEFT_TURN,
			CommandInstruction.RIGHT_TURN, CommandInstruction.U_TURN,
			CommandInstruction.MOVE_BACK, CommandInstruction.POWER_UP,
			CommandInstruction.AGAIN, CommandInstruction.ENERGY_ROUTINE,
			CommandInstruction.SPEED_ROUTINE,
			CommandInstruction.REPEAT_ROUTINE);

	private final CardDeck<ProgrammableCard> drawPile = new CardDeck<>();
	private final List<ProgrammableCard> hand = new ArrayList<>();
	private final List<ProgrammableCard> discardPile = new ArrayList<>();
	private final Register[] registers = new Register[REGISTER_COUNT];
	@Getter
	private boolean submitted;

	public PlayerMat() {
		this(startingDeck());
	}

	/** Uses the given cards as the draw pile, the last card is on top. */
	PlayerMat(List<ProgrammableCard> deck) {
		for (int i = 0; i < registers.length; i++) {
			registers[i] = new Register(i);
		}
		deck.forEach(drawPile::addCard);
	}

	/** The 20 cards every player starts with, in random order. */
	static List<ProgrammableCard> startingDeck() {
		List<ProgrammableCard> deck = new ArrayList<>();
		addCards(deck, CommandInstruction.MOVE_1, 5);
		addCards(deck, CommandInstruction.MOVE_2, 3);
		addCards(deck, CommandInstruction.MOVE_3, 1);
		addCards(deck, CommandInstruction.LEFT_TURN, 3);
		addCards(deck, CommandInstruction.RIGHT_TURN, 3);
		addCards(deck, CommandInstruction.U_TURN, 1);
		addCards(deck, CommandInstruction.MOVE_BACK, 1);
		addCards(deck, CommandInstruction.POWER_UP, 1);
		addCards(deck, CommandInstruction.AGAIN, 2);
		Collections.shuffle(deck);
		return deck;
	}

	private static void addCards(List<ProgrammableCard> deck,
			CommandInstruction instruction, int amount) {
		for (int i = 0; i < amount; i++) {
			deck.add(new ProgrammingCard(instruction));
		}
	}

	/**
	 * Draws until the hand holds nine cards. An empty draw pile is refilled by
	 * shuffling the discard pile into it.
	 */
	public void drawHand() {
		while (hand.size() < HAND_SIZE) {
			if (drawPile.isEmpty()) {
				shuffleDiscardPileIntoDrawPile();
			}
			Optional<ProgrammableCard> card = drawPile.drawCard();
			if (card.isEmpty()) {
				return;
			}
			hand.add(card.get());
		}
	}

	private void shuffleDiscardPileIntoDrawPile() {
		discardPile.forEach(drawPile::addCard);
		discardPile.clear();
		drawPile.shuffle();
	}

	/** Puts a new card in the discard pile, it now cycles like any other. */
	public void addToDiscardPile(ProgrammableCard card) {
		discardPile.add(Objects.requireNonNull(card));
	}

	/**
	 * Moves the chosen cards from the hand to the five registers, discards the
	 * rest of the hand and marks the mat submitted. A second program is
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
		if (instructions.get(0).repeatsPreviousRegister()) {
			throw new IllegalArgumentException(
					instructions.get(0) + " cannot be placed in register 1");
		}
		List<ProgrammableCard> remaining = new ArrayList<>(hand);
		List<ProgrammableCard> chosen = new ArrayList<>();
		for (CommandInstruction instruction : instructions) {
			chosen.add(takeCard(remaining, instruction));
		}
		for (int i = 0; i < REGISTER_COUNT; i++) {
			registers[i].setCard(chosen.get(i));
		}
		discardPile.addAll(remaining);
		hand.clear();
		submitted = true;
	}

	private ProgrammableCard takeCard(List<ProgrammableCard> cards,
			CommandInstruction instruction) {
		for (ProgrammableCard card : cards) {
			if (card instanceof ProgrammingCard programmingCard
					&& programmingCard.getInstruction() == instruction) {
				cards.remove(card);
				return card;
			}
		}
		throw new IllegalArgumentException(
				"Instruction is not in the hand: " + instruction);
	}

	/**
	 * Discards the cards in the registers so the next round can be programmed.
	 */
	public void clearProgram() {
		for (Register register : registers) {
			if (register.getCard() != null) {
				discardPile.add(register.getCard());
				register.setCard(null);
			}
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
