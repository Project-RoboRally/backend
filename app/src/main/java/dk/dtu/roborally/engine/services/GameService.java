package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.engine.activation.Priority;
import dk.dtu.roborally.engine.activation.RoundActivator;
import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.exceptions.game.GameNotFoundException;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.RoundResult;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.repository.GameRepository;

import java.util.List;

/**
 * Service for logic about a game
 *
 * @author Elias, Nicoleta, Matthias, Sebastian
 */
public class GameService {

	private final GameRepository gameRepository;
	private final GameEventPublisher gameEventPublisher;
	private final RoundActivator roundActivator;

	public GameService(GameRepository gameRepository,
			GameEventPublisher gameEventPublisher) {
		this.gameRepository = gameRepository;
		this.gameEventPublisher = gameEventPublisher;
		this.roundActivator = new RoundActivator(gameEventPublisher);
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

		if (game.getGameStatus() == newStatus) {
			return game;
		}

		game.setGameStatus(newStatus);
		gameRepository.update(game);
		gameEventPublisher.publishStatus(game);
		return game;
	}

	public Game finishGame(String gameId) {
		return updateStatus(gameId, GameStatus.FINISHED);
	}

	/**
	 * Registers a game that a lobby has just created and notifies the lobby's
	 * subscribers. The board message includes each robot on its starting space.
	 * Validation already happened in GameLobby.startGame().
	 */
	public void startGame(String lobbyId, Game game) {
		startUpgradePhase(game);
		gameRepository.add(game);
		gameEventPublisher.publishBoard(lobbyId, game);
		gameEventPublisher.publishGameStarted(lobbyId, game);
	}

	/**
	 * Stores this player's five-card program. When every player has submitted,
	 * resolves the round, clears the mats and republishes the board. A finished
	 * game rejects further programs and does not start another upgrade phase.
	 * Returns false while the round is still waiting.
	 */
	public boolean submitProgram(GameLobby lobby, String username,
			List<CommandInstruction> registers) {
		if (!lobby.isGameStarted()) {
			throw new IllegalStateException("The game has not been started.");
		}

		Game game = getGame(lobby.getGameId());
		if (game.getGameStatus() == GameStatus.FINISHED) {
			throw new IllegalStateException("The game has already finished.");
		}
		requirePhase(game, Phase.PROGRAMMING);
		findPlayer(game, username).getPlayerMat().program(registers);

		boolean everyoneSubmitted = game.getPlayersInGame().stream()
				.allMatch(player -> player.getPlayerMat().isSubmitted());
		if (!everyoneSubmitted) {
			gameRepository.update(game);
			return false;
		}

		game.getCurrentRound().setCurrentPhase(Phase.ACTIVATION);
		RoundResult result = activateRound(game);
		for (Player player : game.getPlayersInGame()) {
			player.getPlayerMat().clearProgram();
		}
		gameEventPublisher.publishRound(lobby.getLobbyId(), game, result);
		if (game.getGameStatus() == GameStatus.FINISHED) {
			game.getCurrentRound().setCurrentPhase(Phase.ENDED);
			gameRepository.update(game);
			return true;
		}
		startUpgradePhase(game);
		gameEventPublisher.publishRoundStarted(lobby.getLobbyId(), game);
		gameRepository.update(game);
		return true;
	}

	/**
	 * Buys an upgrade for the player whose turn it is. Routines go straight to
	 * the player's discard pile.
	 */
	public void buyUpgrade(GameLobby lobby, String username, UpgradeType type) {
		Game game = upgradeGame(lobby);
		Player player = currentBuyer(game, username);

		if (player.getEnergy() < type.getCost()) {
			throw new IllegalArgumentException("Not enough energy for " + type);
		}
		game.getUpgradeShop().buy(type);
		player.spendEnergy(type.getCost());
		player.getPlayerMat()
				.addToDiscardPile(new ProgrammingCard(type.getRoutine()));
		endUpgradeTurn(lobby, game);
	}

	/** Passes on buying an upgrade this round. */
	public void skipUpgrade(GameLobby lobby, String username) {
		Game game = upgradeGame(lobby);
		currentBuyer(game, username);
		endUpgradeTurn(lobby, game);
	}

	private Game upgradeGame(GameLobby lobby) {
		if (!lobby.isGameStarted()) {
			throw new IllegalStateException("The game has not been started.");
		}
		Game game = getGame(lobby.getGameId());
		requirePhase(game, Phase.UPGRADE);
		return game;
	}

	private Player currentBuyer(Game game, String username) {
		Player player = findPlayer(game, username);
		if (game.getCurrentBuyer().orElse(null) != player) {
			throw new IllegalStateException(
					"It is not your turn to buy: " + username);
		}
		return player;
	}

	private void endUpgradeTurn(GameLobby lobby, Game game) {
		game.endUpgradeTurn();
		if (game.getCurrentBuyer().isPresent()) {
			gameRepository.update(game);
			gameEventPublisher.publishUpgradeTurn(lobby.getLobbyId(), game);
			return;
		}
		startProgrammingPhase(game);
		gameRepository.update(game);
		gameEventPublisher.publishProgrammingStarted(lobby.getLobbyId(), game);
	}

	/**
	 * Refills the shop and lets players buy in priority antenna order.
	 */
	private void startUpgradePhase(Game game) {
		List<Player> order = Priority.order(game);

		game.getCurrentRound().setCurrentPhase(Phase.UPGRADE);
		game.getUpgradeShop().refill(order.size());
		game.setUpgradeOrder(order);
	}

	private void startProgrammingPhase(Game game) {
		game.getCurrentRound().setCurrentPhase(Phase.PROGRAMMING);
		for (Player player : game.getPlayersInGame()) {
			player.getPlayerMat().drawHand();
		}
	}

	private void requirePhase(Game game, Phase phase) {
		if (game.getCurrentRound().getCurrentPhase() != phase) {
			throw new IllegalStateException(
					"The game is not in the " + phase + " phase.");
		}
	}

	/** Runs the activation phase of the round. */
	RoundResult activateRound(Game game) {
		return roundActivator.activate(game);
	}

	private Player findPlayer(Game game, String username) {
		for (Player player : game.getPlayersInGame()) {
			if (player.getUser().getUsername().equals(username)) {
				return player;
			}
		}
		throw new IllegalArgumentException(
				"Player is not in this game: " + username);
	}

}
