package dk.dtu.roborally.websocket;

import dk.dtu.roborally.api.dto.CreateLobbyDTO;
import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.engine.services.LobbyService;
import dk.dtu.roborally.exceptions.game.GameNotFoundException;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.repository.inmemory.InMemoryGameRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryLobbyRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;
import dk.dtu.roborally.websocket.dto.PlayerGameStateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStateControllerTest {
	private LobbyService lobbyService;
	private PlayerStateController controller;
	private GameLobby lobby;

	@BeforeEach
	void setUp() {
		GameEventPublisher publisher = new GameEventPublisher() {
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
			public void publishChoiceRequired(Game game) {
			}

			@Override
			public void publishBoard(Game game) {
			}
		};
		InMemoryUserRepository users = new InMemoryUserRepository();
		users.add(new User("August"));
		users.add(new User("Alice"));
		users.add(new User("Bob"));

		GameService gameService = new GameService(new InMemoryGameRepository(),
				publisher);
		lobbyService = new LobbyService(new InMemoryLobbyRepository(), users,
				gameService);
		controller = new PlayerStateController(gameService);

		lobby = lobbyService.createLobby(new CreateLobbyDTO("Lobby", "August"));
		lobbyService.joinLobby(lobby.getLobbyId(), "Alice");
		lobbyService.setReady(lobby.getLobbyId(), "Alice", true);
	}

	@Test
	void repliesWithTheSubscribersStateOnceTheGameHasStarted() {
		lobbyService.startGame(lobby.getLobbyId(), "August");

		PlayerGameStateDTO state = controller.playerState(lobby.getGameId(),
				"Alice");

		assertNotNull(state);
		assertEquals(PlayerGameStateDTO.CURRENT_STATE, state.type());
		assertEquals(lobby.getGameId(), state.gameId());
		assertEquals("Alice", state.you());
		assertEquals(2, state.players().size());
		assertNotNull(state.playerMat());
	}

	@Test
	void anUnknownGameIsNotFound() {
		assertThrows(GameNotFoundException.class,
				() -> controller.playerState("missing", "Alice"));
	}

	@Test
	void sendsNothingToAUserWhoIsNotPlayingInTheGame() {
		lobbyService.startGame(lobby.getLobbyId(), "August");

		assertNull(controller.playerState(lobby.getGameId(), "Bob"));
	}
}
