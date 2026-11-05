package dk.dtu.roborally.models.game;

import lombok.Getter;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Represents the round's programming time limit. The first submitted program
 * starts a shared 30-second countdown. Later submissions do not restart it. On
 * expiry, game logic fills remaining empty registers from the affected players'
 * draw piles.
 *
 * @author August, Sebastian
 */
public class ProgrammingTimer {
	@Getter
	private static final Duration TIME_LIMIT = Duration.ofSeconds(30);

	private Instant endsAt;

	/**
	 * Starts the countdown from now, unless it is already running. Returns
	 * whether it started.
	 */
	public boolean start(Instant now) {
		if (endsAt != null) {
			return false;
		}
		endsAt = now.plus(TIME_LIMIT);
		return true;
	}

	/** When programming time runs out, empty while the timer is not running. */
	public Optional<Instant> getEndsAt() {
		return Optional.ofNullable(endsAt);
	}

	public void stop() {
		endsAt = null;
	}

	/** Puts back a running timer loaded from storage. */
	public void restore(Instant endsAt) {
		this.endsAt = endsAt;
	}
}
