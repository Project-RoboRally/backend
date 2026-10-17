package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.exceptions.game.GameNotFoundException;
import dk.dtu.roborally.loaders.JsonLoader;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.repository.inmemory.InMemoryGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameServiceTest {
	private final List<Game> published = new ArrayList<>();
	private final List<Game> startedPublished = new ArrayList<>();
	private final List<String> startedLobbyIds = new ArrayList<>();
	private final List<String> boardLobbyIds = new ArrayList<>();
	private GameService gameService;
	private Game game;

	@BeforeEach
	void setUp() {
		InMemoryGameRepository repository = new InMemoryGameRepository();
		GameEventPublisher publisher = new GameEventPublisher() {
			@Override
			public void publishStatus(Game game) {
				published.add(game);
			}

			@Override
			public void publishBoard(String lobbyId, Board board) {
				boardLobbyIds.add(lobbyId);
			}

			@Override
			public void publishGameStarted(String lobbyId, Game game) {
				startedLobbyIds.add(lobbyId);
				startedPublished.add(game);
			}
		};
		gameService = new GameService(repository, publisher);

		game = new Game("game-1", null, Set.of(), Map.of());
		repository.add(game);
	}

	@Test
	void newGameIsInProgress() {
		assertEquals(GameStatus.IN_PROGRESS, gameService.getStatus("game-1"));
	}

	@Test
	void updatingStatusChangesGameAndPublishesOnce() {
		gameService.updateStatus("game-1", GameStatus.FINISHED);

		assertEquals(GameStatus.FINISHED, game.getGameStatus());
		assertEquals(List.of(game), published);
	}

	@Test
	void updatingToSameStatusDoesNotPublish() {
		gameService.updateStatus("game-1", GameStatus.IN_PROGRESS);

		assertTrue(published.isEmpty());
	}

	@Test
	void finishGameSetsStatusToFinished() {
		gameService.finishGame("game-1");

		assertEquals(GameStatus.FINISHED, gameService.getStatus("game-1"));
	}

	@Test
	void startingGamePublishesGameStartedOnce() {
		GameLobby lobby = new GameLobby(JsonLoader.loadConfig(), "lobby",
				new User("alice"));
		lobby.addUserToLobby(new User("bob"));
		Game newGame = new Game("game-2", null, Set.of(), Map.of());

		gameService.startGame(lobby.getLobbyId(), newGame);

		assertEquals(List.of(newGame), startedPublished);
		assertEquals(List.of(lobby.getLobbyId()), startedLobbyIds);
	}

	@Test
	void startingLobbyWithoutGamePublishesBoard() {
		GameLobby lobby = new GameLobby(JsonLoader.loadConfig(), "lobby",
				new User("alice"));
		lobby.addUserToLobby(new User("bob"));

		gameService.startGame(lobby);

		assertEquals(List.of(lobby.getLobbyId()), boardLobbyIds);
		assertTrue(startedPublished.isEmpty());
	}

	@Test
	void unknownGameThrows() {
		assertThrows(GameNotFoundException.class,
				() -> gameService.updateStatus("missing", GameStatus.FINISHED));
		assertTrue(published.isEmpty());
	}
}
