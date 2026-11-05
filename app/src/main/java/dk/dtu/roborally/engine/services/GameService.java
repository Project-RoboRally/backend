package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.engine.GameClock;
import dk.dtu.roborally.engine.SystemGameClock;
import dk.dtu.roborally.engine.activation.Priority;
import dk.dtu.roborally.engine.activation.RoundActivator;
import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.exceptions.game.GameNotFoundException;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.ProgrammingTimer;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.game.RoundResult;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.repository.GameRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service for logic about a game. Everything that changes a game is
 * synchronized, so two players acting at once, or the programming timer running
 * out, can not change a game at the same time.
 *
 * @author Elias, Nicoleta, Matthias, Sebastian
 */
public class GameService {

	private final GameRepository gameRepository;
	private final GameEventPublisher gameEventPublisher;
	private final RoundActivator roundActivator;
	private final GameClock clock;

	public GameService(GameRepository gameRepository,
			GameEventPublisher gameEventPublisher) {
		this(gameRepository, gameEventPublisher, new SystemGameClock());
	}

	public GameService(GameRepository gameRepository,
			GameEventPublisher gameEventPublisher, GameClock clock) {
		this.gameRepository = gameRepository;
		this.gameEventPublisher = gameEventPublisher;
		this.roundActivator = new RoundActivator(gameEventPublisher);
		this.clock = clock;
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
	public synchronized Game updateStatus(String gameId, GameStatus newStatus) {
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
	public synchronized void startGame(String lobbyId, Game game) {
		startUpgradePhase(game);
		gameRepository.add(game);
		gameEventPublisher.publishBoard(lobbyId, game);
		gameEventPublisher.publishGameStarted(lobbyId, game);
	}

	/**
	 * Stores this player's five-card program, given as card names. When every
	 * player has submitted, activation starts, see {@link #continueActivation}.
	 * A finished game rejects further programs. Returns false while the round
	 * is still waiting for programs.
	 */
	public synchronized boolean submitProgram(GameLobby lobby, String username,
			List<String> registers) {
		if (!lobby.isGameStarted()) {
			throw new IllegalStateException("The game has not been started.");
		}

		Game game = getGame(lobby.getGameId());
		if (game.getGameStatus() == GameStatus.FINISHED) {
			throw new IllegalStateException("The game has already finished.");
		}
		requirePhase(game, Phase.PROGRAMMING);
		findPlayer(game, username).getPlayerMat().programCards(registers);

		boolean everyoneSubmitted = game.getPlayersInGame().stream()
				.allMatch(player -> player.getPlayerMat().isSubmitted());
		if (!everyoneSubmitted) {
			startTimer(lobby, game);
			gameRepository.update(game);
			return false;
		}

		startActivation(lobby, game);
		return true;
	}

	/**
	 * The first program in starts the 30 second timer for everyone else. When
	 * it runs out, see {@link #programmingTimeUp}.
	 */
	private void startTimer(GameLobby lobby, Game game) {
		ProgrammingTimer timer = game.getCurrentRound().getTimer();
		if (!timer.start(clock.now())) {
			return;
		}
		Instant endsAt = timer.getEndsAt().orElseThrow();
		clock.schedule(endsAt, () -> programmingTimeUp(lobby, endsAt));
		gameEventPublisher.publishTimerStarted(lobby.getLobbyId(), game);
	}

	/**
	 * Programming time is up. Anyone still programming has their hand discarded
	 * and their registers filled from the top of their deck, then the round
	 * runs. Does nothing if this timer is not the running one anymore, like
	 * when everyone submitted in time.
	 */
	public synchronized void programmingTimeUp(GameLobby lobby,
			Instant endsAt) {
		Game game = getGame(lobby.getGameId());
		Round round = game.getCurrentRound();
		if (round.getCurrentPhase() != Phase.PROGRAMMING
				|| !round.getTimer().getEndsAt().equals(Optional.of(endsAt))) {
			return;
		}
		for (Player player : game.getPlayersInGame()) {
			if (!player.getPlayerMat().isSubmitted()) {
				player.getPlayerMat().fillRegistersFromDeck();
			}
		}
		startActivation(lobby, game);
	}

	/**
	 * After a restart the saved deadlines are still there but nothing is
	 * waiting for them, so this schedules them again. A deadline that already
	 * passed runs right away.
	 */
	public void resumeTimers(List<GameLobby> lobbies) {
		for (GameLobby lobby : lobbies) {
			if (!lobby.isGameStarted()) {
				continue;
			}
			gameRepository.getById(lobby.getGameId()).flatMap(
					game -> game.getCurrentRound().getTimer().getEndsAt())
					.ifPresent(endsAt -> clock.schedule(endsAt,
							() -> programmingTimeUp(lobby, endsAt)));
		}
	}

	private void startActivation(GameLobby lobby, Game game) {
		game.getCurrentRound().getTimer().stop();
		game.getCurrentRound().setCurrentPhase(Phase.ACTIVATION);
		continueActivation(lobby, game);
	}

	/**
	 * Answers the choice activation is waiting on, then carries on with the
	 * round.
	 */
	public synchronized void makeChoice(GameLobby lobby, String username,
			String choice) {
		if (!lobby.isGameStarted()) {
			throw new IllegalStateException("The game has not been started.");
		}
		Game game = getGame(lobby.getGameId());
		requirePhase(game, Phase.ACTIVATION);
		game.getCurrentRound().answer(findPlayer(game, username).getId(),
				choice);
		continueActivation(lobby, game);
	}

	/**
	 * Runs activation until it is done or waits on a choice. Once the round is
	 * over the mats are cleared and the next round starts, unless someone won.
	 */
	private void continueActivation(GameLobby lobby, Game game) {
		RoundResult result = activateRound(game);
		if (game.getCurrentRound().isActivating()) {
			gameRepository.update(game);
			gameEventPublisher.publishRound(lobby.getLobbyId(), game, result);
			gameEventPublisher.publishChoiceRequired(lobby.getLobbyId(), game);
			return;
		}

		for (Player player : game.getPlayersInGame()) {
			player.getPlayerMat().clearProgram();
		}
		gameEventPublisher.publishRound(lobby.getLobbyId(), game, result);
		if (game.getGameStatus() == GameStatus.FINISHED) {
			game.getCurrentRound().setCurrentPhase(Phase.ENDED);
			gameRepository.update(game);
			return;
		}
		startUpgradePhase(game);
		gameEventPublisher.publishRoundStarted(lobby.getLobbyId(), game);
		gameRepository.update(game);
	}

	/**
	 * Same as {@link #buyUpgrade(GameLobby, String, UpgradeType, UpgradeType)}
	 * with a free slot.
	 */
	public void buyUpgrade(GameLobby lobby, String username, UpgradeType type) {
		buyUpgrade(lobby, username, type, null);
	}

	/**
	 * Buys an upgrade for the player whose turn it is. Routines go straight to
	 * the player's discard pile, anything else gets installed. With all three
	 * slots of that kind taken, replacing names the installed upgrade it
	 * replaces, which is gone after.
	 */
	public synchronized void buyUpgrade(GameLobby lobby, String username,
			UpgradeType type, UpgradeType replacing) {
		Game game = upgradeGame(lobby);
		Player player = currentBuyer(game, username);

		if (player.getEnergy() < type.getCost()) {
			throw new IllegalArgumentException("Not enough energy for " + type);
		}
		if (type.isRoutine()) {
			player.getPlayerMat()
					.addToDiscardPile(new ProgrammingCard(type.getRoutine()));
		} else {
			player.getUpgrades().install(type, replacing);
		}
		game.getUpgradeShop().buy(type);
		player.spendEnergy(type.getCost());
		endUpgradeTurn(lobby, game);
	}

	/** Passes on buying an upgrade this round. */
	public synchronized void skipUpgrade(GameLobby lobby, String username) {
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
