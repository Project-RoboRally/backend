package dk.dtu.roborally.engine;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.engine.services.LobbyService;
import dk.dtu.roborally.engine.services.UserService;
import dk.dtu.roborally.repository.GameRepository;
import dk.dtu.roborally.repository.LobbyRepository;
import dk.dtu.roborally.repository.UserRepository;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Game Engine entry point. Initializes from App.java
 *
 * @author Elias, Victor, Sebastian
 */
public class GameEngine {

	// Services
	@Getter
	private final UserService userService;

	@Getter
	private final GameService gameService;

	@Getter
	private final LobbyService lobbyService;

	public GameEngine(GameRepository gameRepository,
			UserRepository userRepository, LobbyRepository lobbyRepository,
			GameEventPublisher gameEventPublisher) {

		// Setup services
		this.userService = new UserService(userRepository,
				new BCryptPasswordEncoder());
		this.gameService = new GameService(gameRepository, gameEventPublisher);
		this.lobbyService = new LobbyService(lobbyRepository, userRepository,
				gameService);
		gameService.resumeTimers(lobbyRepository.getAll());
	}
}
