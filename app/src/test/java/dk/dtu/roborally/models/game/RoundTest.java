package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.RegisterStage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
