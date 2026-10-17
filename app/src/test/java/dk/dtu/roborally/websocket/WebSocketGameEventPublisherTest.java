package dk.dtu.roborally.websocket;

import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.loaders.JsonLoader;
import dk.dtu.roborally.loaders.LevelLoader;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.Wall;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import dk.dtu.roborally.websocket.dto.BoardDTO;
import dk.dtu.roborally.websocket.dto.BoardElementDTO;
import dk.dtu.roborally.websocket.dto.CardDTO;
import dk.dtu.roborally.websocket.dto.GameStatusDTO;
import dk.dtu.roborally.websocket.dto.PlayerGameStateDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebSocketGameEventPublisherTest {

	@Test
	void publishesStatusToGameTopic() {
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		WebSocketGameEventPublisher publisher = new WebSocketGameEventPublisher(
				template);
		Game game = new Game("game-1", boardWithWall(),
				Set.of(player("alice", PlayerColor.RED, 0, 0),
						player("bob", PlayerColor.BLUE, 1, 0)),
				Map.of());

		publisher.publishStatus(game);

		verify(template).convertAndSend("/game/game-1/status",
				new GameStatusDTO("game-1", GameStatus.FINISHED));
	}

	@Test
	void publishesGameStartedToEachPlayersTopic() {
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		WebSocketGameEventPublisher publisher = new WebSocketGameEventPublisher(
				template);
		Game game = new Game("game-1", boardWithWall(), Set.of(), Map.of());

		publisher.publishGameStarted("lobby-1", game);

		ArgumentCaptor<PlayerGameStateDTO> alice = ArgumentCaptor
				.forClass(PlayerGameStateDTO.class);
		ArgumentCaptor<PlayerGameStateDTO> bob = ArgumentCaptor
				.forClass(PlayerGameStateDTO.class);
		verify(template).convertAndSend(eq("/game/lobby/lobby-1/player/alice"),
				alice.capture());
		verify(template).convertAndSend(eq("/game/lobby/lobby-1/player/bob"),
				bob.capture());

		PlayerGameStateDTO aliceState = alice.getValue();
		assertEquals(PlayerGameStateDTO.GAME_STARTED, aliceState.type());
		assertEquals("alice", aliceState.you());
		assertEquals("game-1", aliceState.gameId());
		assertEquals(Phase.PROGRAMMING, aliceState.round().phase());
		assertEquals(2, aliceState.players().size());
		assertEquals(2, aliceState.board().width());
		assertEquals(1, aliceState.board().height());
		assertEquals(
				List.of(new BoardElementDTO.WallDTO("WALL", Direction.NORTH)),
				aliceState.board().spaces().get(0).elements());
		assertEquals(5, aliceState.playerMat().registers().size());
		assertEquals("bob", bob.getValue().you());
	}

	@Test
	void publishesLoadedLevelToLobbyBoardTopic() {
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		WebSocketGameEventPublisher publisher = new WebSocketGameEventPublisher(
				template);
		Board level = LevelLoader.load(JsonLoader.loadConfig().levels.level_0);

		publisher.publishBoard("lobby-1", level);

		ArgumentCaptor<BoardDTO> sent = ArgumentCaptor.forClass(BoardDTO.class);
		verify(template).convertAndSend(eq("/game/lobby/lobby-1/board"),
				sent.capture());
		assertEquals(level.getSpaceMap().size(),
				sent.getValue().spaces().size());
		assertTrue(sent.getValue().spaces().stream()
				.anyMatch(space -> !space.elements().isEmpty()));
	}

	@Test
	void mapsProgrammingCard() {
		assertEquals(new CardDTO("PROGRAMMING", "MOVE_2"),
				CardDTO.from(new ProgrammingCard(CommandInstruction.MOVE_2)));
	}

	private static Board boardWithWall() {
		Map<Vector2D, Space> spaces = new HashMap<>();
		Space first = new Space(new Vector2D(0, 0));
		first.addBoardElement(new Wall(new Vector2D(0, 0), Direction.NORTH));
		spaces.put(first.getPosition(), first);
		spaces.put(new Vector2D(1, 0), new Space(new Vector2D(1, 0)));
		return new Board(spaces);
	}

	private static Player player(String username, PlayerColor color, int x,
			int y) {
		return new Player(username + "-id", new User(username), color,
				new Robot(new Vector2D(x, y), Direction.EAST));
	}
}
