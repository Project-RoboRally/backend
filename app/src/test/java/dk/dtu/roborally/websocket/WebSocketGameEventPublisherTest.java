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
import dk.dtu.roborally.models.game.RoundResult;
import dk.dtu.roborally.models.game.RoundResult.PlayedCard;
import dk.dtu.roborally.models.game.RoundResult.RegisterReveal;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import dk.dtu.roborally.models.player.RobotView;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebSocketGameEventPublisherTest {

	@Test
	void publishesStatusToGameTopic() {
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		WebSocketGameEventPublisher publisher = new WebSocketGameEventPublisher(
				template);
		Game game = new Game("game-1", boardWithWall(), Set.of(), Map.of());
		game.setGameStatus(GameStatus.FINISHED);

		publisher.publishStatus(game);

		verify(template).convertAndSend("/game/game-1/status",
				new GameStatusDTO("game-1", GameStatus.FINISHED));
	}

	@Test
	void publishesGameStartedToEachPlayersTopic() {
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		WebSocketGameEventPublisher publisher = new WebSocketGameEventPublisher(
				template);
		Game game = new Game("game-1", boardWithWall(),
				Set.of(player("alice", PlayerColor.RED, 0, 0),
						player("bob", PlayerColor.BLUE, 1, 0)),
				Map.of());

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
		assertEquals(Phase.UPGRADE, aliceState.round().phase());
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
	void publishesProgrammingStartedWithTheNewHandToEachPlayersTopic() {
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		WebSocketGameEventPublisher publisher = new WebSocketGameEventPublisher(
				template);
		Player alice = player("alice", PlayerColor.RED, 0, 0);
		Game game = new Game("game-1", boardWithWall(), Set.of(alice),
				Map.of());
		alice.getPlayerMat().drawHand();

		publisher.publishProgrammingStarted("lobby-1", game);

		ArgumentCaptor<PlayerGameStateDTO> sent = ArgumentCaptor
				.forClass(PlayerGameStateDTO.class);
		verify(template).convertAndSend(eq("/game/lobby/lobby-1/player/alice"),
				sent.capture());
		assertEquals(PlayerGameStateDTO.PROGRAMMING_STARTED,
				sent.getValue().type());
		assertEquals(9, sent.getValue().playerMat().hand().size());
	}

	@Test
	void publishesTheShopAndWhoseTurnItIs() {
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		WebSocketGameEventPublisher publisher = new WebSocketGameEventPublisher(
				template);
		Player alice = player("alice", PlayerColor.RED, 0, 0);
		Game game = new Game("game-1", boardWithWall(), Set.of(alice),
				Map.of());
		game.getUpgradeShop().refill(1);
		game.setUpgradeOrder(List.of(alice));

		publisher.publishUpgradeTurn("lobby-1", game);

		ArgumentCaptor<PlayerGameStateDTO> sent = ArgumentCaptor
				.forClass(PlayerGameStateDTO.class);
		verify(template).convertAndSend(eq("/game/lobby/lobby-1/player/alice"),
				sent.capture());
		assertEquals(PlayerGameStateDTO.UPGRADE_TURN, sent.getValue().type());
		assertEquals(1, sent.getValue().upgradeShop().size());
		assertEquals(3, sent.getValue().upgradeShop().get(0).cost());
		assertEquals("alice", sent.getValue().currentBuyer());
	}

	@Test
	void publishesLoadedLevelToLobbyBoardTopic() {
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		WebSocketGameEventPublisher publisher = new WebSocketGameEventPublisher(
				template);
		Board level = LevelLoader.load(JsonLoader.loadConfig().levels.level_0);
		Game game = new Game("game-1", level, Set.of(), Map.of());

		publisher.publishBoard("lobby-1", game);

		ArgumentCaptor<BoardDTO> sent = ArgumentCaptor.forClass(BoardDTO.class);
		verify(template).convertAndSend(eq("/game/lobby/lobby-1/board"),
				sent.capture());
		assertEquals(level.getSpaceMap().size(),
				sent.getValue().spaces().size());
		assertTrue(sent.getValue().spaces().stream()
				.anyMatch(space -> !space.elements().isEmpty()));
		assertTrue(sent.getValue().robots().isEmpty());
	}

	@Test
	void publishesPlacedRobotsOnTheBoardTopic() {
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		WebSocketGameEventPublisher publisher = new WebSocketGameEventPublisher(
				template);
		Game game = new Game("game-1", boardWithWall(),
				Set.of(player("alice", PlayerColor.RED, 0, 0),
						player("bob", PlayerColor.BLUE, 1, 0)),
				Map.of());

		publisher.publishBoard("lobby-1", game);

		ArgumentCaptor<BoardDTO> sent = ArgumentCaptor.forClass(BoardDTO.class);
		verify(template).convertAndSend(eq("/game/lobby/lobby-1/board"),
				sent.capture());
		assertEquals(List.of(
				new BoardDTO.RobotDTO("alice", PlayerColor.RED, 0, 0,
						Direction.EAST),
				new BoardDTO.RobotDTO("bob", PlayerColor.BLUE, 1, 0,
						Direction.EAST)),
				sent.getValue().robots());
		assertTrue(sent.getValue().steps().isEmpty());
		assertTrue(sent.getValue().registers().isEmpty());
	}

	@Test
	void publishesRoundStepsOnTheBoardTopic() {
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		WebSocketGameEventPublisher publisher = new WebSocketGameEventPublisher(
				template);
		Game game = new Game("game-1", boardWithWall(),
				Set.of(player("alice", PlayerColor.RED, 1, 0)), Map.of());

		ProgrammingCard card = new ProgrammingCard(CommandInstruction.MOVE_1);
		publisher.publishRound("lobby-1", game, new RoundResult(
				List.of(List.of(new RobotView("alice", PlayerColor.RED, 1, 0,
						Direction.EAST))),
				List.of(new RegisterReveal(0,
						List.of(new PlayedCard("alice", card, 0, 1))))));

		ArgumentCaptor<BoardDTO> sent = ArgumentCaptor.forClass(BoardDTO.class);
		verify(template).convertAndSend(eq("/game/lobby/lobby-1/board"),
				sent.capture());
		assertEquals(1, sent.getValue().steps().size());
		assertEquals(new BoardDTO.RobotDTO("alice", PlayerColor.RED, 1, 0,
				Direction.EAST), sent.getValue().steps().get(0).get(0));
		assertEquals(
				List.of(new BoardDTO.RegisterDTO(0,
						List.of(new BoardDTO.RegisterDTO.PlayedCardDTO("alice",
								new CardDTO("PROGRAMMING", "MOVE_1"), 0, 1)))),
				sent.getValue().registers());
	}

	@Test
	void otherPlayersCannotSeeASubmittedProgram() {
		SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
		WebSocketGameEventPublisher publisher = new WebSocketGameEventPublisher(
				template);
		Player alice = player("alice", PlayerColor.RED, 0, 0);
		Player bob = player("bob", PlayerColor.BLUE, 1, 0);
		Game game = new Game("game-1", boardWithWall(), Set.of(alice, bob),
				Map.of());
		bob.getPlayerMat().getRegisters().get(0)
				.setCard(new ProgrammingCard(CommandInstruction.MOVE_1));

		publisher.publishRoundStarted("lobby-1", game);

		ArgumentCaptor<PlayerGameStateDTO> sent = ArgumentCaptor
				.forClass(PlayerGameStateDTO.class);
		verify(template).convertAndSend(eq("/game/lobby/lobby-1/player/alice"),
				sent.capture());
		assertTrue(sent.getValue().playerMat().registers().stream()
				.allMatch(register -> register.card() == null));
		assertFalse(sent.getValue().players().toString().contains("MOVE_1"));
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
