package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.api.dto.CreateLobbyDTO;
import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PlayerPresence;
import dk.dtu.roborally.models.game.RoundResult;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.repository.inmemory.InMemoryGameRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryLobbyRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Elias
 */
class PresenceServiceTest {
	private PresenceService presence;
	private Game game;
	private String gameId;
	private final List<List<PlayerPresence>> published = new ArrayList<>();

	@BeforeEach
	void setUp() {
		InMemoryUserRepository users = new InMemoryUserRepository();
		users.add(new User("August"));
		users.add(new User("Alice"));
		users.add(new User("Bob"));

		GameService gameService = new GameService(new InMemoryGameRepository(),
				new SilentPublisher());
		LobbyService lobbyService = new LobbyService(
				new InMemoryLobbyRepository(), users, gameService);
		GameLobby lobby = lobbyService
				.createLobby(new CreateLobbyDTO("Lobby", "August"));
		lobbyService.joinLobby(lobby.getLobbyId(), "Alice");
		lobbyService.setReady(lobby.getLobbyId(), "Alice", true);
		lobbyService.startGame(lobby.getLobbyId(), "August");

		gameId = lobby.getGameId();
		game = gameService.getGame(gameId);
		presence = new PresenceService(gameService,
				(id, players) -> published.add(players));
		// A client subscribing to the presence starts tracking the game
		presence.presence(gameId);
	}

	@Test
	void everyoneStartsOnline() {
		assertTrue(find("August").online());
		assertTrue(find("Alice").online());
	}

	@Test
	void oneMissedTickKeepsAPlayerOnline() {
		presence.tick();

		assertTrue(find("Alice").online());
	}

	@Test
	void twoMissedTicksInARowMakeAPlayerOffline() {
		presence.recordPing(gameId, "August");
		presence.tick();
		presence.recordPing(gameId, "August");
		presence.tick();
		presence.recordPing(gameId, "August");
		presence.tick();

		assertTrue(find("August").online());
		assertFalse(find("Alice").online());
	}

	@Test
	void aPingBetweenMissesResetsTheCount() {
		presence.tick();
		presence.recordPing(gameId, "Alice");
		presence.tick();
		presence.tick();

		assertTrue(find("Alice").online());
	}

	@Test
	void aPingBringsAnOfflinePlayerBackAndPublishesIt() {
		presence.tick();
		presence.tick();
		published.clear();

		presence.recordPing(gameId, "Alice");

		assertTrue(find("Alice").online());
		assertEquals(1, published.size());
	}

	@Test
	void everyTickPublishesThePresence() {
		presence.recordPing(gameId, "August");
		presence.tick();

		assertEquals(1, published.size());
		assertEquals(2, published.get(0).size());
	}

	@Test
	void pingsFromUsersOutsideTheGameAreIgnored() {
		presence.recordPing(gameId, "Bob");

		assertEquals(2, presence.presence(gameId).size());
	}

	@Test
	void anOfflinePlayerTheGameWaitsForIsBlocking() {
		game.getCurrentRound().setCurrentPhase(Phase.PROGRAMMING);
		presence.tick();
		presence.tick();

		assertTrue(find("Alice").blocking());
	}

	@Test
	void anOfflinePlayerTheGameDoesNotWaitForIsNotBlocking() {
		game.getCurrentRound().setCurrentPhase(Phase.ACTIVATION);
		presence.tick();
		presence.tick();

		assertFalse(find("Alice").online());
		assertFalse(find("Alice").blocking());
	}

	@Test
	void anOnlinePlayerIsNeverBlocking() {
		game.getCurrentRound().setCurrentPhase(Phase.PROGRAMMING);

		assertFalse(find("Alice").blocking());
	}

	private PlayerPresence find(String username) {
		return presence.presence(gameId).stream()
				.filter(p -> p.username().equals(username)).findFirst()
				.orElseThrow();
	}

	private static class SilentPublisher implements GameEventPublisher {
		@Override
		public void publishStatus(Game game) {
		}

		@Override
		public void publishGameStarted(Game game) {
		}

		@Override
		public void publishRoundStarted(Game game) {
		}

		@Override
		public void publishUpgradeTurn(Game game) {
		}

		@Override
		public void publishProgrammingStarted(Game game) {
		}

		@Override
		public void publishUpgradeUsed(Game game) {
		}

		@Override
		public void publishTimerStarted(Game game) {
		}

		@Override
		public void publishChoiceRequired(Game game, RoundResult result) {
		}

		@Override
		public void publishBoard(Game game) {
		}
	}
}
