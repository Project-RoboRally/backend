package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.Phase;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * Represents an active or ended round
 *
 * @author August
 */

public class Round {
	@Getter
	@Setter
	private Phase currentPhase = Phase.UPGRADE;

	@Getter
	private final ProgrammingTimer timer = new ProgrammingTimer();

	private Integer currentRegisterIndex;

	public Optional<Integer> getCurrentRegisterIndex() {
		return Optional.ofNullable(currentRegisterIndex);
	}

	// TODO: Add round management logic
}
