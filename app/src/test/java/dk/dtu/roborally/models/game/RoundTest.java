package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.RegisterStage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundTest {

	@Test
	void walksThroughTheActivationOrderOfARegister() {
		Round round = new Round();
		round.startRegister(2, List.of("a", "b"));

		assertTrue(round.isActivating());
		assertEquals(Optional.of(2), round.getCurrentRegisterIndex());
		assertEquals(RegisterStage.CARDS, round.getStage());
		assertEquals(Optional.of("a"), round.getNextToActivate());
		round.markActivated();
		assertEquals(Optional.of("b"), round.getNextToActivate());
		round.markActivated();
		assertEquals(Optional.empty(), round.getNextToActivate());
	}

	@Test
	void finishingClearsTheProgress() {
		Round round = new Round();
		round.startRegister(4, List.of("a"));

		round.finishActivation();

		assertFalse(round.isActivating());
		assertEquals(Optional.empty(), round.getCurrentRegisterIndex());
		assertEquals(List.of(), round.getActivationOrder());
	}

	@Test
	void waitsForAChoiceUntilItIsAnswered() {
		Round round = new Round();
		PendingChoice choice = new PendingChoice("a", ChoiceType.WEASEL_ACTION,
				List.of("LEFT_TURN", "RIGHT_TURN"));

		assertEquals(Optional.empty(), round.ask(choice));
		assertEquals(Optional.of(choice), round.getPendingChoice());

		round.answer("a", "RIGHT_TURN");

		assertEquals(Optional.empty(), round.getPendingChoice());
		assertEquals(Optional.of("RIGHT_TURN"), round.ask(choice));
		assertEquals(Optional.empty(), round.getPendingChoice());
	}

	@Test
	void onlyTheAskedPlayerCanAnswerWithAnOption() {
		Round round = new Round();
		round.ask(new PendingChoice("a", ChoiceType.WEASEL_ACTION,
				List.of("LEFT_TURN")));

		assertThrows(IllegalStateException.class,
				() -> round.answer("b", "LEFT_TURN"));
		assertThrows(IllegalArgumentException.class,
				() -> round.answer("a", "MOVE_3"));
	}

	@Test
	void answeringWithNothingPendingFails() {
		assertThrows(IllegalStateException.class,
				() -> new Round().answer("a", "LEFT_TURN"));
	}

	@Test
	void beginNextCountsEachRoundOfTheGame() {
		Round round = new Round();

		assertEquals(0, round.getNumber());
		round.beginNext();
		assertEquals(1, round.getNumber());
		round.beginNext();
		assertEquals(2, round.getNumber());
	}
}
