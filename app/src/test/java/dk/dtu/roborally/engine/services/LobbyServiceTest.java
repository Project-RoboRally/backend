package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.api.dto.CreateLobbyDTO;
import dk.dtu.roborally.exceptions.lobby.NotAuthorizedException;
import dk.dtu.roborally.exceptions.lobby.UserNotInLobby;
import dk.dtu.roborally.exceptions.user.UserNotFound;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.repository.inmemory.InMemoryGameRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryLobbyRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LobbyServiceTest {
	private InMemoryGameRepository gameRepository;
	private LobbyService lobbyService;
	private GameLobby lobby;

	@BeforeEach
	void setUp() {
		gameRepository = new InMemoryGameRepository();
		InMemoryUserRepository userRepository = new InMemoryUserRepository();
		lobbyService = new LobbyService(new InMemoryLobbyRepository(),
				userRepository, gameRepository);

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
	void startingTheGamePersistsIt() {
		lobbyService.setReady(lobby.getLobbyId(), "Alice", true);

		Game game = lobbyService.startGame(lobby.getLobbyId(), "August");

		assertTrue(gameRepository.getById(game.getGameID()).isPresent());
		assertEquals(game.getGameID(), lobby.getGameId());
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
}
