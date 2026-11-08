package dk.dtu.roborally.models.game;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgrammingTimerTest {

	private static final Instant NOW = Instant.parse("2026-11-05T12:00:00Z");

	@Test
	void runsForThirtySecondsFromTheFirstStart() {
		ProgrammingTimer timer = new ProgrammingTimer();

		assertTrue(timer.start(NOW));
		assertFalse(timer.start(NOW.plusSeconds(10)));

		assertEquals(Optional.of(NOW.plusSeconds(30)), timer.getEndsAt());
	}

	@Test
	void stoppingClearsTheDeadline() {
		ProgrammingTimer timer = new ProgrammingTimer();
		timer.start(NOW);

		timer.stop();

		assertEquals(Optional.empty(), timer.getEndsAt());
		assertTrue(timer.start(NOW));
	}
}
