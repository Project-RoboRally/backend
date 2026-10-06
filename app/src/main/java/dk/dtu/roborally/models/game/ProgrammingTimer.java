package dk.dtu.roborally.models.game;

import java.time.Duration;
import java.time.Instant;

import lombok.Getter;

/**
 * Represents the round's programming time limit. The first submitted program
 * starts a shared countdown. Later submissions do not restart it.
 *
 * @author August, Matthias
 */
public class ProgrammingTimer {

	@Getter
	private final Duration timeLimit;

	@Getter
	private Instant startedAt;

	public ProgrammingTimer() {
		this(Duration.ofSeconds(30));
	}

	public ProgrammingTimer(Duration timeLimit) {
		if (timeLimit == null || timeLimit.isZero() || timeLimit.isNegative()) {
			throw new IllegalArgumentException("Time limit must be positive.");
		}

		this.timeLimit = timeLimit;
	}
}
