package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.repository.GameRepository;

/**
 * Service for logic about a game
 *
 * @author Elias
 */
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }
}
