package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.exceptions.game.GameNotFoundException;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.repository.inmemory.InMemoryGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;



class GameServiceTest {
	private final List<Game> published = new ArrayList<>();
	private GameService gameService;
	private Game game;

	@BeforeEach
	void setUp() {
		InMemoryGameRepository repository = new InMemoryGameRepository();
		GameEventPublisher publisher = published::add;
		gameService = new GameService(repository, publisher);

		game = new Game("game-1", null, Map.of());
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
	void unknownGameThrows() {
		assertThrows(GameNotFoundException.class,
				() -> gameService.updateStatus("missing", GameStatus.FINISHED));
		assertTrue(published.isEmpty());
	}
}
