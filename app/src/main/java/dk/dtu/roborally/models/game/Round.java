package dk.dtu.roborally.models.game;

import java.util.Optional;

import dk.dtu.roborally.enums.Phase;
import lombok.Getter;

/**
 * Represents an active or ended round.
 *
 * @author August, Sebastian
 */
public class Round {
	@Getter
	private Phase currentPhase = Phase.PROGRAMMING;

	@Getter
	private final ProgrammingTimer timer = new ProgrammingTimer();

	@Getter
	private int number;

	private Integer currentRegisterIndex;

	public Round() {
		this(1);
	}

	public Round(int number) {
		this.number = number;
		this.currentRegisterIndex = 0;
	}

	public Optional<Integer> getCurrentRegisterIndex() {
		return Optional.ofNullable(currentRegisterIndex);
	}

	public int getCurrentRegister() {
		return currentRegisterIndex == null ? 0 : currentRegisterIndex;
	}

	public void advanceRegister() {
		// Registers are indexed from 0 to 4, so index 4 is the fifth and final
		// register.
		if (currentRegisterIndex == null) {
			currentRegisterIndex = 0;
		}
		if (currentRegisterIndex < 4) {
			currentRegisterIndex++;
		}
	}

	public void resetRegister() {
		currentRegisterIndex = 0;
	}
}
