package dk.dtu.roborally.engine;

import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.engine.services.PlayerService;
import dk.dtu.roborally.models.Server;
import dk.dtu.roborally.repository.GameRepository;
import dk.dtu.roborally.repository.PlayerRepository;
import lombok.Getter;

/**
 * Game Engine entry point. Initializes from App.java
 *
 * @author Elias, Victor
 */
public class GameEngine {

	// Services
	@Getter
	private final PlayerService playerService;
	@Getter
	private final GameService gameService;

	public GameEngine(GameRepository gameRepository,
			PlayerRepository playerRepository) {

        // Setup services
        this.playerService = new PlayerService(playerRepository);
        this.gameService = new GameService(gameRepository);
        this.server = new Server();
    }

}
