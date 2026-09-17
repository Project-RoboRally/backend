package dk.dtu.roborally.engine;

/**
 * Provides timing information for the game loop.
 * @author Sebastian
 */
public class GameClock {

    private long previousTime;

    public GameClock() {
        previousTime = System.nanoTime();
    }

    public double deltaTime() {
        long currentTime = System.nanoTime();
        double deltaTime = (currentTime - previousTime) / 1_000_000_000.0;
        previousTime = currentTime;
        return deltaTime;
    }
}
