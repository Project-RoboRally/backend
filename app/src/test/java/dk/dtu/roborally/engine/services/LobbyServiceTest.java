package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.api.dto.CreateLobbyDTO;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.exceptions.lobby.NotAuthorizedException;
import dk.dtu.roborally.exceptions.lobby.UserNotInLobby;
import dk.dtu.roborally.exceptions.user.UserNotFound;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.repository.inmemory.InMemoryGameRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryLobbyRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;
import dk.dtu.roborally.engine.publishers.GameEventPublisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LobbyServiceTest {
	private InMemoryGameRepository gameRepository;
	private InMemoryLobbyRepository lobbyRepository;
	private LobbyService lobbyService;
	private GameLobby lobby;
	private GameEventPublisher publisher;

	@BeforeEach
	void setUp() {
		gameRepository = new InMemoryGameRepository();
		InMemoryUserRepository userRepository = new InMemoryUserRepository();
		publisher = mock(GameEventPublisher.class);
		lobbyService = new LobbyService(new InMemoryLobbyRepository(),
				userRepository, new GameService(gameRepository, publisher));

		userRepository.add(new User("August"));
		userRepository.add(new User("Alice"));
		userRepository.add(new User("Outsider"));

		lobby = lobbyService.createLobby(new CreateLobbyDTO("Lobby", "August"));
		lobbyService.joinLobby(lobby.getLobbyId(), "Alice");
	}

	@Test
	void onlyTheOwnerCanStartTheGame() {
		lobbyService.setReady(lobby.getLobbyId(), "Alice", true);

		assertThrows(NotAuthorizedException.class,
				() -> lobbyService.startGame(lobby.getLobbyId(), "Alice"));
	}

	@Test
	void startingTheGamePersistsItAndNotifiesTheLobby() {
		lobbyService.setReady(lobby.getLobbyId(), "Alice", true);

		lobbyService.startGame(lobby.getLobbyId(), "August");
		String gameId = lobby.getGameId();
		Game game = gameRepository.getById(gameId).orElseThrow();

		assertTrue(gameRepository.getById(game.getGameID()).isPresent());
		assertEquals(game.getGameID(), lobby.getGameId());

		assertTrue(lobby.isGameStarted());
		verify(publisher).publishGameStarted(lobby.getLobbyId(), game);
	}

	@Test
	void settingReadyForUnknownUserThrows() {
		assertThrows(UserNotFound.class, () -> lobbyService
				.setReady(lobby.getLobbyId(), "NoSuchUser", true));
	}

	@Test
	void settingReadyForUserOutsideTheLobbyThrows() {
		assertThrows(UserNotInLobby.class, () -> lobbyService
				.setReady(lobby.getLobbyId(), "Outsider", true));
	}

	@Test
	void settingColorChangesTheMembersColor() {
		lobbyService.setColor(lobby.getLobbyId(), "Alice", PlayerColor.PURPLE);

		assertTrue(lobby.getMembers().stream()
				.anyMatch(member -> member.user().getUsername().equals("Alice")
						&& member.color() == PlayerColor.PURPLE));
	}

	@Test
	void settingColorForUserOutsideTheLobbyThrows() {
		assertThrows(UserNotInLobby.class, () -> lobbyService
				.setColor(lobby.getLobbyId(), "Outsider", PlayerColor.PURPLE));
	}
}
