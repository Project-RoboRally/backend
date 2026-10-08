package dk.dtu.roborally.models.game;

import java.util.Optional;

import dk.dtu.roborally.enums.Phase;
import lombok.Getter;

/**
 * Represents an active or ended round.
 *
 * @author August, Matthias
 */
public class Round {

	private static final int REGISTER_COUNT = 5;

	@Getter
	private final int roundNumber;

	@Getter
	private Phase currentPhase = Phase.PROGRAMMING;

	@Getter
	private final ProgrammingTimer timer = new ProgrammingTimer();

	private Integer currentRegisterNumber;

	public Round(int roundNumber) {
		if (roundNumber < 1) {
			throw new IllegalArgumentException(
					"Round number must be at least 1.");
		}

		this.roundNumber = roundNumber;
	}

	public Optional<Integer> getCurrentRegisterNumber() {
		return Optional.ofNullable(currentRegisterNumber);
	}

	public void startActivationPhase() {
		if (currentPhase != Phase.PROGRAMMING) {
			throw new IllegalStateException(
					"Activation phase can only start from programming phase.");
		}

		currentPhase = Phase.ACTIVATION;
		currentRegisterNumber = 1;
	}

	public boolean advanceRegister() {
		if (currentPhase != Phase.ACTIVATION) {
			throw new IllegalStateException(
					"Registers can only advance during activation phase.");
		}

		if (currentRegisterNumber == null) {
			throw new IllegalStateException(
					"No register is currently active.");
		}

		if (isLastRegister()) {
			return false;
		}

		currentRegisterNumber++;
		return true;
	}

	public boolean isLastRegister() {
		return currentRegisterNumber != null
				&& currentRegisterNumber == REGISTER_COUNT;
	}
}