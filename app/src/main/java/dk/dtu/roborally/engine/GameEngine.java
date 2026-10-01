package dk.dtu.roborally.engine;

import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.engine.services.LobbyService;
import dk.dtu.roborally.engine.services.UserService;
import dk.dtu.roborally.models.Server;
import dk.dtu.roborally.repository.GameRepository;
import dk.dtu.roborally.repository.LobbyRepository;
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
	@Getter
	private final LobbyService lobbyService;

	private Server server;

	public GameEngine(GameRepository gameRepository,
			UserRepository userRepository, LobbyRepository lobbyRepository) {

		// Setup services
		this.userService = new UserService(userRepository);
		this.gameService = new GameService(gameRepository);
		this.lobbyService = new LobbyService(lobbyRepository, userRepository);
		this.server = new Server();
	}

}
