package dk.dtu.roborally.engine;

import java.time.Instant;

/**
 * Tells the time and runs things later. The game uses it for the programming
 * timer, and tests swap it for one they control.
 *
 * @author Sebastian
 */
public interface GameClock {

	Instant now();

	/** Runs the task at the given time, or right away if that has passed. */
	void schedule(Instant at, Runnable task);
}
