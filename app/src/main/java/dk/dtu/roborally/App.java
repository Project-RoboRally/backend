package dk.dtu.roborally;

import dk.dtu.roborally.engine.GameEngine;
import dk.dtu.roborally.repository.GameRepository;
import dk.dtu.roborally.repository.UserRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryGameRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;
import lombok.Getter;

/**
 * Main class for the application. Here we intialize the game engine and start
 * the Spring API.
 *
 * @author Elias & Nicoleta
 */

public class App {

	@Getter
	private final GameEngine gameEngine;

	public App(String[] args) {
		// Initialise repositories
		GameRepository gameRepository = new InMemoryGameRepository();
		UserRepository playerRepository = new InMemoryUserRepository();

		// Setup GameEngine
		this.gameEngine = new GameEngine(gameRepository, playerRepository);

		// Start Backend API
		BackendApplication.main(args);
	}
}
