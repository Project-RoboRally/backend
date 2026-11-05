package dk.dtu.roborally.engine;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The real clock. Scheduled tasks run on one background thread that does not
 * keep the application alive.
 *
 * @author Sebastian
 */
public class SystemGameClock implements GameClock {

	private final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor(task -> {
				Thread thread = new Thread(task, "game-clock");
				thread.setDaemon(true);
				return thread;
			});

	@Override
	public Instant now() {
		return Instant.now();
	}

	@Override
	public void schedule(Instant at, Runnable task) {
		long delay = Math.max(0, Duration.between(now(), at).toMillis());
		executor.schedule(task, delay, TimeUnit.MILLISECONDS);
	}
}
