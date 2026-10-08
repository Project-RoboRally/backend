package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.exceptions.game.GameNotFoundException;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.repository.GameRepository;

/**
 * Service for logic about a game
 *
 * @author Elias, Nicoleta
 */
public class GameService {

	private final GameRepository gameRepository;
	private final GameEventPublisher gameEventPublisher;

	public GameService(GameRepository gameRepository,
			GameEventPublisher gameEventPublisher) {
		this.gameRepository = gameRepository;
		this.gameEventPublisher = gameEventPublisher;
	}

	public Game getGame(String gameId) {
		return gameRepository.getById(gameId)
				.orElseThrow(() -> new GameNotFoundException(gameId));
	}

	public GameStatus getStatus(String gameId) {
		return getGame(gameId).getGameStatus();
	}

	/**
	 * Changes the status of a game and notifies subscribers. Does nothing if
	 * the game already has the given status.
	 */
	public Game updateStatus(String gameId, GameStatus newStatus) {
		Game game = getGame(gameId);
		if (game.getGameStatus() == newStatus)
			return game;

		game.setGameStatus(newStatus);
		gameEventPublisher.publishStatus(game);
		return game;
	}

	public Game finishGame(String gameId) {
		return updateStatus(gameId, GameStatus.FINISHED);
	}
}
