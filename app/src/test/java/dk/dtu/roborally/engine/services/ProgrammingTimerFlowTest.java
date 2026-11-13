package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.api.dto.CreateLobbyDTO;
import dk.dtu.roborally.engine.GameClock;
import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.repository.inmemory.InMemoryGameRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryLobbyRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ProgrammingTimerFlowTest {

	private static final Instant NOW = Instant.parse("2026-11-05T12:00:00Z");

	private final List<Runnable> scheduled = new ArrayList<>();
	private final List<Instant> scheduledAt = new ArrayList<>();
	private final GameEventPublisher publisher = mock(GameEventPublisher.class);
	private GameService gameService;
	private GameLobby lobby;
	private Game game;

	@BeforeEach
	void setUp() {
		GameClock clock = new GameClock() {
			@Override
			public Instant now() {
				return NOW;
			}

			@Override
			public void schedule(Instant at, Runnable task) {
				scheduledAt.add(at);
				scheduled.add(task);
			}
		};
		gameService = new GameService(new InMemoryGameRepository(), publisher,
				clock);
		InMemoryUserRepository users = new InMemoryUserRepository();
		users.add(new User("August"));
		users.add(new User("Alice"));
		LobbyService lobbies = new LobbyService(new InMemoryLobbyRepository(),
				users, gameService);
		lobby = lobbies.createLobby(new CreateLobbyDTO("Lobby", "August"));
		lobbies.joinLobby(lobby.getLobbyId(), "Alice");
		lobbies.setReady(lobby.getLobbyId(), "Alice", true);
		lobbies.startGame(lobby.getLobbyId(), "August");
		game = gameService.getGame(lobby.getGameId());
		while (game.getCurrentBuyer().isPresent()) {
			gameService.skipUpgrade(lobby.getGameId(),
					game.getCurrentBuyer().get().getUser().getUsername());
		}
		// Turns only from here on, so nobody falls off and stops the round
		turnsOnly(named("August"));
		turnsOnly(named("Alice"));
	}

	@Test
	void theFirstProgramStartsTheTimerOnce() {
		gameService.submitProgram(lobby.getGameId(), "August", rightTurns());

		assertEquals(List.of(NOW.plusSeconds(30)), scheduledAt);
		assertEquals(Optional.of(NOW.plusSeconds(30)),
				game.getCurrentRound().getTimer().getEndsAt());
		verify(publisher, times(1)).publishTimerStarted(game);
	}

	@Test
	void whenTimeRunsOutTheDeckFillsTheRegistersAndTheRoundRuns() {
		gameService.submitProgram(lobby.getGameId(), "August", rightTurns());
		Player alice = named("Alice");

		scheduled.get(0).run();

		// Round over, so back in the upgrade phase with the timer stopped
		assertEquals(Phase.UPGRADE, game.getCurrentRound().getCurrentPhase());
		assertEquals(Optional.empty(),
				game.getCurrentRound().getTimer().getEndsAt());
		// Her 9 right turns in hand were discarded and the 5 left turns on
		// her deck were played. Lasers may have added SPAM, so count by name
		assertEquals(9, count(alice, "RIGHT_TURN"));
		assertEquals(5, count(alice, "LEFT_TURN"));
	}

	@Test
	void anOldTimerDoesNothingOnceEveryoneSubmitted() {
		gameService.submitProgram(lobby.getGameId(), "August", rightTurns());
		gameService.submitProgram(lobby.getGameId(), "Alice", rightTurns());
		Phase phase = game.getCurrentRound().getCurrentPhase();

		scheduled.get(0).run();

		assertEquals(phase, game.getCurrentRound().getCurrentPhase());
		assertTrue(game.getCurrentRound().getTimer().getEndsAt().isEmpty());
	}

	@Test
	void aRunningTimerIsScheduledAgainAfterARestart() {
		gameService.submitProgram(lobby.getGameId(), "August", rightTurns());
		scheduled.clear();
		scheduledAt.clear();

		gameService.resumeTimers(List.of(lobby));

		assertEquals(List.of(NOW.plusSeconds(30)), scheduledAt);
		scheduled.get(0).run();
		assertEquals(Phase.UPGRADE, game.getCurrentRound().getCurrentPhase());
	}

	private Player named(String username) {
		return game.getPlayersInGame().stream().filter(
				player -> player.getUser().getUsername().equals(username))
				.findFirst().orElseThrow();
	}

	private static long count(Player player, String card) {
		return player.getPlayerMat().getDiscardPile().stream()
				.filter(discarded -> discarded.getName().equals(card)).count();
	}

	private static List<String> rightTurns() {
		return List.of("RIGHT_TURN", "RIGHT_TURN", "RIGHT_TURN", "RIGHT_TURN",
				"RIGHT_TURN");
	}

	/** Nine turns in hand and five more on the deck. */
	private static void turnsOnly(Player player) {
		List<ProgrammableCard> hand = new ArrayList<>();
		List<ProgrammableCard> deck = new ArrayList<>();
		for (int i = 0; i < 9; i++) {
			hand.add(new ProgrammingCard(CommandInstruction.RIGHT_TURN));
		}
		for (int i = 0; i < 5; i++) {
			deck.add(new ProgrammingCard(CommandInstruction.LEFT_TURN));
		}
		List<ProgrammableCard> registers = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			registers.add(null);
		}
		player.getPlayerMat().restore(deck, hand, List.of(), registers, false);
	}
}
