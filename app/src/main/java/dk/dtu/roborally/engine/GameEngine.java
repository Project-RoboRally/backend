package dk.dtu.roborally.engine;

import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.engine.services.UserService;
import dk.dtu.roborally.models.Server;
import dk.dtu.roborally.repository.GameRepository;
import dk.dtu.roborally.repository.UserRepository;
import lombok.Getter;

/**
 * Game Engine entry point. Initializes from App.java
 *
 * @author Elias, Victor
 */
public class GameEngine {

	// Services
	@Getter
	private final UserService userService;
	@Getter
	private final GameService gameService;

	private Server server;

	public GameEngine(GameRepository gameRepository,
			UserRepository userRepository) {

		// Setup services
		this.userService = new UserService(userRepository);
		this.gameService = new GameService(gameRepository);
		this.server = new Server();
	}

}
