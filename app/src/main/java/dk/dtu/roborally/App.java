package dk.dtu.roborally;

import dk.dtu.roborally.engine.GameEngine;

/**
 * Main class for the application.
 * Here we intialize the game engine and start the Spring API.
 * 
 * @author Elias & Nicoleta
 */
public class App {

    private final GameEngine gameEngine;

    public App(String[] args) {
        this.gameEngine = new GameEngine();
        BackendApplication.main(args);
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }
}
