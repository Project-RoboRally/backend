package dk.dtu.roborally.models.game;

import java.util.Optional;

import dk.dtu.roborally.enums.Phase;
import lombok.Getter;

/**
 * Represents an active or ended round
 *
 * @author August
 */

public class Round {
	@Getter
	private Phase currentPhase = Phase.PROGRAMMING;

	@Getter
	private final ProgrammingTimer timer = new ProgrammingTimer();

	private Integer currentRegisterIndex;

	public Optional<Integer> getCurrentRegisterIndex() {
		return Optional.ofNullable(currentRegisterIndex);
	}

	// TODO: Add round management logic
}
