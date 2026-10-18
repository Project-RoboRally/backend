package dk.dtu.roborally.models.player;

import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerMatTest {

	@Test
	void allowListIsOnlyMove1Move2AndRightTurn() {
		assertEquals(Set.of(CommandInstruction.MOVE_1,
				CommandInstruction.MOVE_2, CommandInstruction.RIGHT_TURN),
				PlayerMat.ALLOWED);

		PlayerMat mat = new PlayerMat();
		mat.program(List.of(CommandInstruction.MOVE_1,
				CommandInstruction.MOVE_2, CommandInstruction.RIGHT_TURN,
				CommandInstruction.MOVE_1, CommandInstruction.MOVE_2));

		assertTrue(mat.isSubmitted());
		assertEquals(CommandInstruction.MOVE_2, card(mat, 1).getInstruction());
		assertEquals(CommandInstruction.RIGHT_TURN,
				card(mat, 2).getInstruction());

		for (CommandInstruction instruction : CommandInstruction.values()) {
			if (PlayerMat.ALLOWED.contains(instruction)) {
				continue;
			}
			PlayerMat rejected = new PlayerMat();
			List<CommandInstruction> program = new ArrayList<>();
			program.add(instruction);
			program.addAll(List.of(CommandInstruction.MOVE_1,
					CommandInstruction.MOVE_1, CommandInstruction.MOVE_1,
					CommandInstruction.MOVE_1));

			assertThrows(IllegalArgumentException.class,
					() -> rejected.program(program));
			assertFalse(rejected.isSubmitted());
			assertNull(rejected.getRegisters().get(0).getCard());
		}
	}

	@Test
	void secondSubmitIsRejectedUntilTheRoundIsCleared() {
		PlayerMat mat = new PlayerMat();
		mat.program(five(CommandInstruction.MOVE_1));

		assertThrows(IllegalStateException.class,
				() -> mat.program(five(CommandInstruction.MOVE_2)));
		assertEquals(CommandInstruction.MOVE_1, card(mat, 0).getInstruction());

		mat.clearProgram();

		assertFalse(mat.isSubmitted());
		assertNull(mat.getRegisters().get(0).getCard());
		mat.program(five(CommandInstruction.RIGHT_TURN));
		assertTrue(mat.isSubmitted());
	}

	private static ProgrammingCard card(PlayerMat mat, int register) {
		return assertInstanceOf(ProgrammingCard.class,
				mat.getRegisters().get(register).getCard());
	}

	private static List<CommandInstruction> five(
			CommandInstruction instruction) {
		return List.of(instruction, instruction, instruction, instruction,
				instruction);
	}
}
