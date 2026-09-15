package dk.dtu.roborally.engine;

import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.engine.services.PlayerService;
import dk.dtu.roborally.repository.GameRepository;
import dk.dtu.roborally.repository.PlayerRepository;

/**
 * Game Engine entry point.
 * Initializes from App.java
 *
 * @param playerService Services
 * @author Elias
 */
public record GameEngine(GameService gameService, PlayerService playerService) {

    public GameEngine(GameRepository gameService, PlayerRepository playerService) {

        // Setup services
        this.playerService = new PlayerService(playerService);
        this.gameService = new GameService(gameService);
    }

}
