package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.Checkpoint;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import dk.dtu.roborally.models.player.RobotView;
import dk.dtu.roborally.repository.inmemory.InMemoryGameRepository;
import dk.dtu.roborally.websocket.PlayerStateController;
import dk.dtu.roborally.websocket.WebSocketGameEventPublisher;
import dk.dtu.roborally.websocket.dto.GameStatusDTO;
import dk.dtu.roborally.websocket.dto.PlayerGameStateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests checkpoint rules, winning, outgoing player state, and reload state.
 */
class CheckpointWinningTest {
	private InMemoryGameRepository repository;
	private SimpMessagingTemplate messages;
	private GameService service;

	@BeforeEach
	void setUp() {
		repository = new InMemoryGameRepository();
		messages = mock(SimpMessagingTemplate.class);
		service = new GameService(repository,
				new WebSocketGameEventPublisher(messages));
	}

	@Test
	void endingRegistersOnConsecutiveCheckpointsAdvancesProgress() {
		Player alice = player("alice", 0, 0, PlayerColor.RED);
		Game game = game(board(1, 2, 3), alice);

		activate(game, List.of(CommandInstruction.MOVE_1,
				CommandInstruction.MOVE_1, CommandInstruction.RIGHT_TURN,
				CommandInstruction.RIGHT_TURN, CommandInstruction.RIGHT_TURN));

		assertEquals(2, alice.getCheckpointsReached());
		assertEquals(GameStatus.IN_PROGRESS, game.getGameStatus());
		assertNull(game.getWinner());
	}

	@Test
	void stayingOnAnAlreadyCollectedCheckpointDoesNotCountItAgain() {
		Player alice = player("alice", 1, 0, PlayerColor.RED);
		Game game = game(board(1, 2), alice);

		activate(game, turns());
		activate(game, turns());

		assertEquals(1, alice.getCheckpointsReached());
		assertEquals(GameStatus.IN_PROGRESS, game.getGameStatus());
		assertNull(game.getWinner());
	}

	@Test
	void laterCheckpointDoesNotCountBeforeTheFirstOne() {
		Player alice = player("alice", 2, 0, PlayerColor.RED);
		Game game = game(board(1, 2), alice);

		activate(game, turns());

		assertEquals(0, alice.getCheckpointsReached());
		assertNull(game.getWinner());
		assertEquals(GameStatus.IN_PROGRESS, game.getGameStatus());
	}

	@Test
	void passingThroughCheckpointDuringMove2DoesNotCollectIt() {
		Player alice = player("alice", 0, 0, PlayerColor.RED);
		Game game = game(board(1, 3), alice);

		activate(game, List.of(CommandInstruction.MOVE_2,
				CommandInstruction.RIGHT_TURN, CommandInstruction.RIGHT_TURN,
				CommandInstruction.RIGHT_TURN, CommandInstruction.RIGHT_TURN));

		assertEquals(new Vector2D(2, 0), alice.getRobot().getCurrentSpace());
		assertEquals(0, alice.getCheckpointsReached());
		assertEquals(GameStatus.IN_PROGRESS, game.getGameStatus());
	}

	@Test
	void finalCheckpointRecordsWinnerAndStopsRemainingRegisters() {
		Player alice = player("alice", 0, 0, PlayerColor.RED);
		Game game = game(board(1, 2), alice);

		List<List<RobotView>> frames = activate(game,
				List.of(CommandInstruction.MOVE_1, CommandInstruction.MOVE_1,
						CommandInstruction.MOVE_1,
						CommandInstruction.RIGHT_TURN,
						CommandInstruction.RIGHT_TURN));

		assertEquals(2, alice.getCheckpointsReached());
		assertSame(alice, game.getWinner());
		assertEquals(GameStatus.FINISHED, game.getGameStatus());
		assertEquals(new Vector2D(2, 0), alice.getRobot().getCurrentSpace());
		assertEquals(Direction.EAST, alice.getRobot().getOrientation());
		assertEquals(2, frames.size());
		verify(messages).convertAndSend("/game/test-game/status",
				new GameStatusDTO("test-game", GameStatus.FINISHED, "alice"));
	}

	@Test
	void finishedGameRejectsNewProgramsWithoutChangingTheRobot() {
		Player alice = player("alice", 0, 0, PlayerColor.RED);
		Game game = game(board(1), alice);
		GameLobby lobby = lobby(game);
		readyToProgram(game, alice);
		assertTrue(service.submitProgram(lobby, "alice", names(moves())));

		IllegalStateException error = assertThrows(IllegalStateException.class,
				() -> service.submitProgram(lobby, "alice", names(moves())));

		assertEquals("The game has already finished.", error.getMessage());
		assertFalse(alice.getPlayerMat().isSubmitted());
		assertEquals(new Vector2D(1, 0), alice.getRobot().getCurrentSpace());
		assertEquals(1, alice.getCheckpointsReached());
		assertEquals(Phase.ENDED, game.getCurrentRound().getCurrentPhase());
	}

	@Test
	void winningRoundSendsProgressWinnerAndAnimationToBothPlayers() {
		Player alice = player("alice", 0, 0, PlayerColor.RED);
		Player bob = player("bob", 0, 1, PlayerColor.BLUE);
		Game game = game(board(1), alice, bob);
		GameLobby lobby = lobby(game);
		readyToProgram(game, alice, bob);

		assertFalse(service.submitProgram(lobby, "alice", names(moves())));
		assertTrue(service.submitProgram(lobby, "bob", names(turns())));

		for (String username : List.of("alice", "bob")) {
			ArgumentCaptor<PlayerGameStateDTO> sent = ArgumentCaptor
					.forClass(PlayerGameStateDTO.class);
			// Timer started, then the round itself
			verify(messages, times(2)).convertAndSend(
					eq("/game/lobby/test-lobby/player/" + username),
					sent.capture());
			assertEquals(PlayerGameStateDTO.TIMER_STARTED,
					sent.getAllValues().get(0).type());
			PlayerGameStateDTO state = sent.getValue();
			assertEquals(username, state.you());
			assertEquals(PlayerGameStateDTO.CURRENT_STATE, state.type());
			assertEquals(GameStatus.FINISHED, state.status());
			assertEquals("alice", state.winnerUsername());
			assertEquals(1,
					state.players().stream()
							.filter(player -> player.username().equals("alice"))
							.findFirst().orElseThrow().checkpointsReached());
			assertEquals(0,
					state.players().stream()
							.filter(player -> player.username().equals("bob"))
							.findFirst().orElseThrow().checkpointsReached());
			assertFalse(state.board().steps().isEmpty());
			assertTrue(state.players().stream()
					.noneMatch(player -> player.programSubmitted()));
			assertTrue(state.playerMat().registers().stream()
					.allMatch(register -> register.card() == null));
		}
	}

	@Test
	void reloadReturnsWinnerAndProgressWithoutReplayingAnimations() {
		Player alice = player("alice", 0, 0, PlayerColor.RED);
		Player bob = player("bob", 0, 1, PlayerColor.BLUE);
		Game game = game(board(1), alice, bob);
		GameLobby lobby = lobby(game);
		readyToProgram(game, alice, bob);
		service.submitProgram(lobby, "alice", names(moves()));
		service.submitProgram(lobby, "bob", names(turns()));
		LobbyService lobbies = mock(LobbyService.class);
		when(lobbies.getLobby("test-lobby")).thenReturn(lobby);
		PlayerStateController controller = new PlayerStateController(lobbies,
				service);

		for (String username : List.of("alice", "bob")) {
			PlayerGameStateDTO state = controller.playerState("test-lobby",
					username);

			assertNotNull(state);
			assertEquals(username, state.you());
			assertEquals(PlayerGameStateDTO.CURRENT_STATE, state.type());
			assertEquals(GameStatus.FINISHED, state.status());
			assertEquals("alice", state.winnerUsername());
			assertEquals(1,
					state.players().stream()
							.filter(player -> player.username().equals("alice"))
							.findFirst().orElseThrow().checkpointsReached());
			assertTrue(state.board().steps().isEmpty());
			assertEquals(1,
					state.board().robots().stream()
							.filter(robot -> robot.username().equals("alice"))
							.findFirst().orElseThrow().x());
		}
	}

	@Test
	void boardWithoutCheckpointsDoesNotDeclareAWinner() {
		Player alice = player("alice", 0, 0, PlayerColor.RED);
		Game game = game(board(), alice);

		activate(game, turns());

		assertEquals(0, alice.getCheckpointsReached());
		assertNull(game.getWinner());
		assertEquals(GameStatus.IN_PROGRESS, game.getGameStatus());
		verifyNoInteractions(messages);
	}

	private Game game(Board board, Player... players) {
		Game game = new Game("test-game", board, Set.of(players));
		repository.add(game);
		return game;
	}

	private GameLobby lobby(Game game) {
		GameLobby lobby = mock(GameLobby.class);
		when(lobby.isGameStarted()).thenReturn(true);
		when(lobby.getGameId()).thenReturn(game.getGameID());
		when(lobby.getLobbyId()).thenReturn("test-lobby");
		return lobby;
	}

	private List<List<RobotView>> activate(Game game,
			List<CommandInstruction> program) {
		for (Player player : game.getPlayersInGame()) {
			player.getPlayerMat().clearProgram();
			hold(player, program);
			player.getPlayerMat().program(program);
		}
		return service.activateRound(game).steps();
	}

	/**
	 * Programs are dealt from the hand, and a new round starts in the upgrade
	 * phase. Gives each player the cards they will submit and opens
	 * programming.
	 */
	private static void readyToProgram(Game game, Player... players) {
		game.getCurrentRound().setCurrentPhase(Phase.PROGRAMMING);
		hold(players[0], moves());
		for (int i = 1; i < players.length; i++) {
			hold(players[i], turns());
		}
	}

	private static void hold(Player player,
			List<CommandInstruction> instructions) {
		for (CommandInstruction instruction : instructions) {
			player.getPlayerMat().addToHand(new ProgrammingCard(instruction));
		}
	}

	/**
	 * Creates two rows with consecutively numbered checkpoints at the given x
	 * positions.
	 */
	private static Board board(int... checkpointPositions) {
		Map<Vector2D, Space> spaces = new HashMap<>();
		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 5; x++) {
				Vector2D position = new Vector2D(x, y);
				spaces.put(position, new Space(position));
			}
		}
		for (int i = 0; i < checkpointPositions.length; i++) {
			Vector2D position = new Vector2D(checkpointPositions[i], 0);
			spaces.get(position)
					.addBoardElement(new Checkpoint(position, i + 1));
		}
		return new Board(spaces);
	}

	private static Player player(String name, int x, int y, PlayerColor color) {
		return new Player(name, new User(name), color,
				new Robot(new Vector2D(x, y), Direction.EAST));
	}

	private static List<CommandInstruction> turns() {
		return java.util.Collections.nCopies(5, CommandInstruction.RIGHT_TURN);
	}

	private static List<CommandInstruction> moves() {
		return java.util.Collections.nCopies(5, CommandInstruction.MOVE_1);
	}

	private static List<String> names(List<CommandInstruction> instructions) {
		return instructions.stream().map(CommandInstruction::name).toList();
	}
}
