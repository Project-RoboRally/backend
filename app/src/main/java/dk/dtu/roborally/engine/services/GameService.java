package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.exceptions.game.GameNotFoundException;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.repository.GameRepository;

/**
 * Service for logic relating to game and round orchestration.
 *
 * @author Elias, Matthias
 */
public class GameService {

	private final GameRepository gameRepository;

	public GameService(GameRepository gameRepository) {
		this.gameRepository = gameRepository;
	}

	public Game getGame(String gameId) {
		return gameRepository.getById(gameId)
				.orElseThrow(() -> new GameNotFoundException(gameId));
	}

	public void startActivationPhase(String gameId) {
		Game game = getGame(gameId);
		game.getCurrentRound().startActivationPhase();
	}

	public void advanceRegister(String gameId) {
		Game game = getGame(gameId);
		Round round = game.getCurrentRound();

		if (round.isLastRegister()) {
			game.startNextRound();
			return;
		}

		round.advanceRegister();
	}
}