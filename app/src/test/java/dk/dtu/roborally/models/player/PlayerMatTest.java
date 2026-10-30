package dk.dtu.roborally.models.player;

import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerMatTest {

	private static final List<CommandInstruction> PROGRAM = List.of(
			CommandInstruction.MOVE_1, CommandInstruction.MOVE_2,
			CommandInstruction.RIGHT_TURN, CommandInstruction.MOVE_1,
			CommandInstruction.MOVE_2);

	@Test
	void allowListHoldsEveryStandardCard() {
		assertEquals(Set.of(CommandInstruction.values()), PlayerMat.ALLOWED);
	}

	@Test
	void programUsesTheCardsFromTheHandAndDiscardsTheRest() {
		PlayerMat mat = stacked();
		ProgrammableCard moved = mat.getHand().get(0);

		mat.program(PROGRAM);

		assertTrue(mat.isSubmitted());
		assertSame(moved, mat.getRegisters().get(0).getCard());
		assertEquals(CommandInstruction.MOVE_2, card(mat, 1).getInstruction());
		assertTrue(mat.getHand().isEmpty());
		assertEquals(4, mat.getDiscardPile().size());
	}

	@Test
	void programNeedsTheCardsToBeInTheHand() {
		PlayerMat mat = stacked();

		assertThrows(IllegalArgumentException.class,
				() -> mat.program(five(CommandInstruction.MOVE_2)));

		assertFalse(mat.isSubmitted());
		assertNull(mat.getRegisters().get(0).getCard());
		assertEquals(9, mat.getHand().size());
		assertTrue(mat.getDiscardPile().isEmpty());
	}

	@Test
	void againIsRejectedInRegisterOneAndAllowedLater() {
		PlayerMat mat = stacked();

		assertThrows(IllegalArgumentException.class,
				() -> mat.program(List.of(CommandInstruction.AGAIN,
						CommandInstruction.MOVE_1, CommandInstruction.MOVE_1,
						CommandInstruction.MOVE_1, CommandInstruction.MOVE_1)));
		assertFalse(mat.isSubmitted());
		assertNull(mat.getRegisters().get(0).getCard());

		mat.program(List.of(CommandInstruction.MOVE_1, CommandInstruction.AGAIN,
				CommandInstruction.MOVE_1, CommandInstruction.MOVE_1,
				CommandInstruction.MOVE_2));
		assertTrue(mat.isSubmitted());
	}

	@Test
	void secondSubmitIsRejectedUntilTheRoundIsCleared() {
		PlayerMat mat = stacked();
		mat.program(PROGRAM);

		assertThrows(IllegalStateException.class, () -> mat.program(PROGRAM));
		assertEquals(CommandInstruction.MOVE_1, card(mat, 0).getInstruction());

		mat.clearProgram();

		assertFalse(mat.isSubmitted());
		assertNull(mat.getRegisters().get(0).getCard());
		assertEquals(9, mat.getDiscardPile().size());
	}

	@Test
	void playerStartsWithTheStandardDeckAndNineCardsInHand() {
		PlayerMat mat = new PlayerMat();

		assertEquals(9, mat.getHand().size());
		assertEquals(11, mat.getDrawPileSize());
		assertTrue(mat.getDiscardPile().isEmpty());

		Map<CommandInstruction, Integer> counts = new EnumMap<>(
				CommandInstruction.class);
		for (ProgrammableCard card : PlayerMat.startingDeck()) {
			counts.merge(((ProgrammingCard) card).getInstruction(), 1,
					Integer::sum);
		}
		assertEquals(Map.of(CommandInstruction.MOVE_1, 5,
				CommandInstruction.MOVE_2, 3, CommandInstruction.MOVE_3, 1,
				CommandInstruction.LEFT_TURN, 3, CommandInstruction.RIGHT_TURN,
				3, CommandInstruction.U_TURN, 1, CommandInstruction.MOVE_BACK,
				1, CommandInstruction.POWER_UP, 1, CommandInstruction.AGAIN, 2),
				counts);
	}

	@Test
	void everyRoundDrawsANewHandAndKeepsAllTwentyCards() {
		PlayerMat mat = new PlayerMat();

		for (int round = 0; round < 10; round++) {
			mat.program(playable(mat));
			mat.clearProgram();
			mat.drawHand();

			assertEquals(9, mat.getHand().size());
			assertEquals(20, mat.getHand().size() + mat.getDrawPileSize()
					+ mat.getDiscardPile().size());
		}
	}

	@Test
	void emptyDrawPileIsRefilledFromTheDiscardPile() {
		PlayerMat mat = stacked();
		mat.program(PROGRAM);
		mat.clearProgram();
		assertEquals(0, mat.getDrawPileSize());

		mat.drawHand();

		assertEquals(9, mat.getHand().size());
		assertTrue(mat.getDiscardPile().isEmpty());
	}

	private static ProgrammingCard card(PlayerMat mat, int register) {
		return assertInstanceOf(ProgrammingCard.class,
				mat.getRegisters().get(register).getCard());
	}

	// A mat holding these nine cards, with an empty draw pile
	private static PlayerMat stacked() {
		List<ProgrammableCard> deck = new ArrayList<>();
		for (CommandInstruction instruction : List.of(CommandInstruction.MOVE_1,
				CommandInstruction.MOVE_2, CommandInstruction.RIGHT_TURN,
				CommandInstruction.MOVE_1, CommandInstruction.MOVE_2,
				CommandInstruction.MOVE_1, CommandInstruction.AGAIN,
				CommandInstruction.MOVE_1, CommandInstruction.LEFT_TURN)) {
			deck.add(0, new ProgrammingCard(instruction));
		}
		return new PlayerMat(deck);
	}

	// Five cards from the hand, none of them Again in the first register
	private static List<CommandInstruction> playable(PlayerMat mat) {
		List<CommandInstruction> program = new ArrayList<>();
		for (ProgrammableCard card : mat.getHand()) {
			program.add(((ProgrammingCard) card).getInstruction());
		}
		program.sort(Comparator.comparing(
				instruction -> instruction == CommandInstruction.AGAIN));
		return program.subList(0, 5);
	}

	private static List<CommandInstruction> five(
			CommandInstruction instruction) {
		return List.of(instruction, instruction, instruction, instruction,
				instruction);
	}
}
