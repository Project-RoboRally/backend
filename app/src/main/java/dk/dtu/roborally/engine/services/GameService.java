package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.models.GameLobby;
import dk.dtu.roborally.models.game.Game;
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

	public void startGame(GameLobby lobby) {
		if (lobby.isGameStarted()) {
			return;
		}

		if (!lobby.hasEnoughPlayers()) {
			throw new IllegalStateException(
					"Not enough players to start the game.");
		}

		Game game = lobby.getGame();
		if (game != null) {
			startGame(lobby, game);
			return;
		}

		lobby.setGameStarted(true);
	}

	public void startGame(GameLobby lobby, Game game) {
		if (!lobby.hasEnoughPlayers()) {
			throw new IllegalStateException(
					"Not enough players to start the game.");
		}

		game.start();
		lobby.setGame(game);
		lobby.setGameStarted(true);
		gameRepository.add(game);
	}
}
