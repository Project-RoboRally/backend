package dk.dtu.roborally;

import dk.dtu.roborally.engine.GameEngine;
import dk.dtu.roborally.repository.GameRepository;
import dk.dtu.roborally.repository.PlayerRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryGameRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryPlayerRepository;
import lombok.Getter;

/**
 * Main class for the application.
 * Here we intialize the game engine and start the Spring API.
 *
 * @author Elias & Nicoleta
 */

public class App {

    @Getter
    private final GameEngine gameEngine;

    public String getGreeting() {
        return "Hello World!";
    }

    public App(String[] args) {
        // Initialise repositories
        GameRepository gameRepository = new InMemoryGameRepository();
        PlayerRepository playerRepository = new InMemoryPlayerRepository();

        // Setup GameEngine
        this.gameEngine = new GameEngine(gameRepository, playerRepository);

        // Start Backend API
        BackendApplication.main(args);
    }

    public void run() {
        System.out.println(getGreeting());

        while (true) {
            // GameLoop
        }
    }
}
