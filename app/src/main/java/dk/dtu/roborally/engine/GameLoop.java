package dk.dtu.roborally.engine;

/**
 * Coordinates periodic game updates.
 * @author Sebastian
 */
public class GameLoop {

    private final GameClock clock;

    public GameLoop() {
        this.clock = new GameClock();
    }

    public double update() {
        return clock.deltaTime();
    }
}
