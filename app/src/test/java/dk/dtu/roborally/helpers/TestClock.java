package dk.dtu.roborally.helpers;

import dk.dtu.roborally.engine.GameClock;

import java.time.Duration;
import java.time.Instant;

// A clock for tests that only moves when the test says so.

public class TestClock implements GameClock {
	private Instant now = Instant.parse("2026-01-01T12:00:00Z");

	public void advance(Duration duration) {
		now = now.plus(duration);
	}

	@Override
	public Instant now() {
		return now;
	}

	@Override
	public void schedule(Instant at, Runnable task) {
		// not needed: sessions and login locks only compare times
	}
}
