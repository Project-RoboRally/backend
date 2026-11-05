package dk.dtu.roborally.models.player;

import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.DamageInstruction;
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
 * @author Matthias, August, Sebastian, Elias
 */
public class PlayerMat {

	public static final int REGISTER_COUNT = 5;
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
			CommandInstruction.SPEED_ROUTINE, CommandInstruction.REPEAT_ROUTINE,
			CommandInstruction.SANDBOX_ROUTINE,
			CommandInstruction.WEASEL_ROUTINE);

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

	/** Puts a card in the hand so it can be programmed. */
	public void addToHand(ProgrammableCard card) {
		hand.add(Objects.requireNonNull(card));
	}

	/** Same as {@link #programCards(List)} for programs without damage. */
	public void program(List<CommandInstruction> instructions) {
		programCards(instructions == null
				? null
				: instructions.stream().map(CommandInstruction::name).toList());
	}

	/**
	 * Moves the chosen cards from the hand to the five registers, discards the
	 * rest of the hand and marks the mat submitted. Cards are named like MOVE_1
	 * or SPAM, so damage cards can be programmed too. A second program is
	 * rejected until {@link #clearProgram()} runs.
	 */
	public void programCards(List<String> cards) {
		if (submitted) {
			throw new IllegalStateException(
					"Program has already been submitted");
		}
		if (cards == null || cards.size() != REGISTER_COUNT) {
			throw new IllegalArgumentException("A program must contain exactly "
					+ REGISTER_COUNT + " instructions");
		}
		for (String card : cards) {
			if (!isAllowed(card)) {
				throw new IllegalArgumentException(
						"Instruction is not allowed: " + card);
			}
		}
		if (repeatsPreviousRegister(cards.get(0))) {
			throw new IllegalArgumentException(
					cards.get(0) + " cannot be placed in register 1");
		}
		List<ProgrammableCard> remaining = new ArrayList<>(hand);
		List<ProgrammableCard> chosen = new ArrayList<>();
		for (String card : cards) {
			chosen.add(takeCard(remaining, card));
		}
		for (int i = 0; i < REGISTER_COUNT; i++) {
			registers[i].setCard(chosen.get(i));
		}
		discardPile.addAll(remaining);
		hand.clear();
		submitted = true;
	}

	private static boolean isAllowed(String card) {
		for (CommandInstruction instruction : ALLOWED) {
			if (instruction.name().equals(card)) {
				return true;
			}
		}
		for (DamageInstruction instruction : DamageInstruction.values()) {
			if (instruction.name().equals(card)) {
				return true;
			}
		}
		return false;
	}

	private static boolean repeatsPreviousRegister(String card) {
		return ALLOWED.stream()
				.anyMatch(instruction -> instruction.name().equals(card)
						&& instruction.repeatsPreviousRegister());
	}

	private ProgrammableCard takeCard(List<ProgrammableCard> cards,
			String name) {
		for (ProgrammableCard card : cards) {
			if (card.getName().equals(name)) {
				cards.remove(card);
				return card;
			}
		}
		throw new IllegalArgumentException(
				"Instruction is not in the hand: " + name);
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

	public List<ProgrammableCard> getDrawPile() {
		return drawPile.getCards();
	}

	/**
	 * Replaces every pile and register. Used when a saved game is loaded. Draw
	 * pile order is bottom to top, and the last card is drawn next. Empty
	 * registers are null.
	 */
	public void restore(List<ProgrammableCard> draw,
			List<ProgrammableCard> handCards,
			List<ProgrammableCard> discardCards,
			List<ProgrammableCard> registerCards, boolean submitted) {
		Objects.requireNonNull(draw);
		Objects.requireNonNull(handCards);
		Objects.requireNonNull(discardCards);
		Objects.requireNonNull(registerCards);
		if (registerCards.size() != REGISTER_COUNT) {
			throw new IllegalArgumentException(
					"A player mat has " + REGISTER_COUNT + " registers");
		}
		drawPile.clear();
		draw.forEach(drawPile::addCard);
		hand.clear();
		hand.addAll(handCards);
		discardPile.clear();
		discardPile.addAll(discardCards);
		for (int i = 0; i < REGISTER_COUNT; i++) {
			registers[i].setCard(registerCards.get(i));
		}
		this.submitted = submitted;
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
