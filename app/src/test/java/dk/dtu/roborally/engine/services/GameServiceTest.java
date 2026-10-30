package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.api.dto.CreateLobbyDTO;
import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.exceptions.game.GameNotFoundException;
import dk.dtu.roborally.loaders.JsonLoader;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.PriorityAntenna;
import dk.dtu.roborally.models.board.elements.Wall;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.RoundResult;
import dk.dtu.roborally.models.game.RoundResult.PlayedCard;
import dk.dtu.roborally.models.game.RoundResult.RegisterReveal;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Register;
import dk.dtu.roborally.models.player.Robot;
import dk.dtu.roborally.models.player.RobotView;
import dk.dtu.roborally.repository.inmemory.InMemoryGameRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryLobbyRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameServiceTest {
	private final List<Game> published = new ArrayList<>();
	private final List<Game> startedPublished = new ArrayList<>();
	private final List<String> startedLobbyIds = new ArrayList<>();
	private final List<String> boardLobbyIds = new ArrayList<>();
	private final List<String> roundStartedLobbyIds = new ArrayList<>();
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
			public void publishBoard(String lobbyId, Game game) {
				boardLobbyIds.add(lobbyId);
			}

			@Override
			public void publishRoundStarted(String lobbyId, Game game) {
				roundStartedLobbyIds.add(lobbyId);
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
		assertEquals(List.of(lobby.getLobbyId()), boardLobbyIds);
	}

	@Test
	void unknownGameThrows() {
		assertThrows(GameNotFoundException.class,
				() -> gameService.updateStatus("missing", GameStatus.FINISHED));
		assertTrue(published.isEmpty());
	}

	@Test
	void wallOnTheCrossedEdgeBlocksTheStep() {
		Player leaving = player("ada", 0, 0, Direction.EAST, PlayerColor.RED);
		run(game(
				board(space(0, 0, new Wall(new Vector2D(0, 0), Direction.EAST)),
						space(1, 0)),
				leaving), five(CommandInstruction.MOVE_1));
		assertEquals(new Vector2D(0, 0), leaving.getRobot().getCurrentSpace());

		Player entering = player("bea", 0, 0, Direction.EAST, PlayerColor.BLUE);
		run(game(
				board(space(0, 0),
						space(1, 0,
								new Wall(new Vector2D(1, 0), Direction.WEST))),
				entering), five(CommandInstruction.MOVE_1));
		assertEquals(new Vector2D(0, 0), entering.getRobot().getCurrentSpace());
	}

	@Test
	void blockedMove2StopsAfterTheFirstStep() {
		Player mover = player("ada", 0, 0, Direction.EAST, PlayerColor.RED);
		run(game(
				board(space(0, 0), space(1, 0),
						space(2, 0,
								new Wall(new Vector2D(2, 0), Direction.WEST))),
				mover), five(CommandInstruction.MOVE_2));

		assertEquals(new Vector2D(1, 0), mover.getRobot().getCurrentSpace());
		assertEquals(Direction.EAST, mover.getRobot().getOrientation());
	}

	@Test
	void leftTurnRotatesWithoutLeavingTheSpace() {
		Player mover = player("ada", 1, 1, Direction.NORTH, PlayerColor.RED);
		run(game(board(space(1, 1)), mover),
				five(CommandInstruction.LEFT_TURN));

		Direction facing = Direction.NORTH;
		for (int i = 0; i < 5; i++) {
			facing = facing.turnLeft();
		}
		assertEquals(facing, mover.getRobot().getOrientation());
		assertEquals(new Vector2D(1, 1), mover.getRobot().getCurrentSpace());
	}

	@Test
	void activationRecordsOneFramePerTurnOrStep() {
		Player mover = player("ada", 0, 0, Direction.EAST, PlayerColor.RED);
		Game round = game(
				board(space(0, 0), space(1, 0), space(2, 0), space(2, -1)),
				mover);
		load(mover,
				List.of(CommandInstruction.MOVE_2, CommandInstruction.LEFT_TURN,
						CommandInstruction.MOVE_1, CommandInstruction.MOVE_1,
						CommandInstruction.MOVE_1));

		List<List<RobotView>> frames = gameService.activateRound(round).steps();

		assertEquals(4, frames.size());
		assertEquals(1, frames.get(0).get(0).x());
		assertEquals(2, frames.get(1).get(0).x());
		assertEquals(Direction.NORTH, frames.get(2).get(0).orientation());
		assertEquals(-1, frames.get(3).get(0).y());
	}

	@Test
	void rightTurnRotatesWithoutLeavingTheSpace() {
		Player mover = player("ada", 1, 1, Direction.NORTH, PlayerColor.RED);
		run(game(board(space(1, 1)), mover),
				five(CommandInstruction.RIGHT_TURN));

		Direction facing = Direction.NORTH;
		for (int i = 0; i < 5; i++) {
			facing = facing.turnRight();
		}
		assertEquals(facing, mover.getRobot().getOrientation());
		assertEquals(new Vector2D(1, 1), mover.getRobot().getCurrentSpace());
	}

	@Test
	void closestRobotToTheAntennaMovesFirst() {
		Player near = player("zzz", 1, 0, Direction.EAST, PlayerColor.RED);
		Player far = player("aaa", 3, 0, Direction.WEST, PlayerColor.BLUE);
		List<CommandInstruction> registers = List.of(CommandInstruction.MOVE_1,
				CommandInstruction.RIGHT_TURN, CommandInstruction.RIGHT_TURN,
				CommandInstruction.RIGHT_TURN, CommandInstruction.RIGHT_TURN);
		Game round = game(board(space(0, 0, new PriorityAntenna()), space(1, 0),
				space(2, 0), space(3, 0)), near, far);

		load(near, registers);
		load(far, registers);
		gameService.activateRound(round);

		assertEquals(new Vector2D(2, 0), near.getRobot().getCurrentSpace());
		assertEquals(new Vector2D(3, 0), far.getRobot().getCurrentSpace());
	}

	@Test
	void usernameBreaksTiesForAntennaDistance() {
		Player alice = player("alice", 0, 1, Direction.EAST, PlayerColor.RED);
		Player carol = player("carol", 1, 0, Direction.SOUTH, PlayerColor.BLUE);
		List<CommandInstruction> registers = List.of(CommandInstruction.MOVE_1,
				CommandInstruction.RIGHT_TURN, CommandInstruction.RIGHT_TURN,
				CommandInstruction.RIGHT_TURN, CommandInstruction.RIGHT_TURN);
		Game round = game(board(space(0, 0, new PriorityAntenna()), space(0, 1),
				space(1, 0), space(1, 1)), alice, carol);

		load(alice, registers);
		load(carol, registers);
		gameService.activateRound(round);

		assertEquals(new Vector2D(1, 1), alice.getRobot().getCurrentSpace());
		assertEquals(new Vector2D(1, 0), carol.getRobot().getCurrentSpace());
	}

	@Test
	void againWithNothingToRepeatThrowsDuringActivation() {
		Player mover = player("ada", 0, 0, Direction.NORTH, PlayerColor.RED);
		Game round = game(board(space(0, 0)), mover);
		mover.getPlayerMat().getRegisters().get(0)
				.setCard(new ProgrammingCard(CommandInstruction.AGAIN));

		assertThrows(IllegalStateException.class,
				() -> gameService.activateRound(round));
		assertEquals(new Vector2D(0, 0), mover.getRobot().getCurrentSpace());
	}

	@Test
	void uTurnRotatesWithoutLeavingTheSpace() {
		Player mover = player("ada", 1, 1, Direction.NORTH, PlayerColor.RED);
		run(game(board(space(1, 1)), mover),
				List.of(CommandInstruction.U_TURN, CommandInstruction.U_TURN,
						CommandInstruction.U_TURN, CommandInstruction.POWER_UP,
						CommandInstruction.POWER_UP));

		assertEquals(Direction.SOUTH, mover.getRobot().getOrientation());
		assertEquals(new Vector2D(1, 1), mover.getRobot().getCurrentSpace());
	}

	@Test
	void moveBackStepsBehindWithoutTurning() {
		Player mover = player("ada", 2, 0, Direction.EAST, PlayerColor.RED);
		run(game(board(space(0, 0), space(1, 0), space(2, 0)), mover), List.of(
				CommandInstruction.MOVE_BACK, CommandInstruction.MOVE_BACK,
				CommandInstruction.MOVE_BACK, CommandInstruction.POWER_UP,
				CommandInstruction.POWER_UP));

		assertEquals(new Vector2D(0, 0), mover.getRobot().getCurrentSpace());
		assertEquals(Direction.EAST, mover.getRobot().getOrientation());
	}

	@Test
	void wallBehindTheRobotBlocksMoveBack() {
		Player mover = player("ada", 1, 0, Direction.EAST, PlayerColor.RED);
		run(game(
				board(space(0, 0),
						space(1, 0,
								new Wall(new Vector2D(1, 0), Direction.WEST))),
				mover), five(CommandInstruction.MOVE_BACK));

		assertEquals(new Vector2D(1, 0), mover.getRobot().getCurrentSpace());
	}

	@Test
	void powerUpGivesOneEnergyAndNoFrame() {
		Player mover = player("ada", 0, 0, Direction.NORTH, PlayerColor.RED);
		assertEquals(5, mover.getEnergy());

		Game round = game(board(space(0, 0)), mover);
		load(mover, five(CommandInstruction.POWER_UP));

		assertTrue(gameService.activateRound(round).steps().isEmpty());
		assertEquals(10, mover.getEnergy());
	}

	@Test
	void againRepeatsThePreviousRegister() {
		Player mover = player("ada", 0, 0, Direction.EAST, PlayerColor.RED);
		run(game(board(space(0, 0), space(1, 0), space(2, 0), space(3, 0)),
				mover),
				List.of(CommandInstruction.MOVE_1, CommandInstruction.AGAIN,
						CommandInstruction.POWER_UP, CommandInstruction.AGAIN,
						CommandInstruction.POWER_UP));

		assertEquals(new Vector2D(2, 0), mover.getRobot().getCurrentSpace());
		assertEquals(8, mover.getEnergy());
	}

	@Test
	void chainedAgainRepeatsTheLastRealInstruction() {
		Player mover = player("ada", 0, 0, Direction.EAST, PlayerColor.RED);
		run(game(board(space(0, 0), space(1, 0), space(2, 0), space(3, 0)),
				mover),
				List.of(CommandInstruction.MOVE_1, CommandInstruction.AGAIN,
						CommandInstruction.AGAIN, CommandInstruction.POWER_UP,
						CommandInstruction.POWER_UP));

		assertEquals(new Vector2D(3, 0), mover.getRobot().getCurrentSpace());
	}

	@Test
	void speedRoutineMovesThreeSpaces() {
		Player mover = player("ada", 0, 0, Direction.EAST, PlayerColor.RED);
		run(game(board(space(0, 0), space(1, 0), space(2, 0), space(3, 0)),
				mover),
				List.of(CommandInstruction.SPEED_ROUTINE,
						CommandInstruction.POWER_UP,
						CommandInstruction.POWER_UP,
						CommandInstruction.POWER_UP,
						CommandInstruction.POWER_UP));

		assertEquals(new Vector2D(3, 0), mover.getRobot().getCurrentSpace());
	}

	@Test
	void energyRoutineGivesOneEnergy() {
		Player mover = player("ada", 0, 0, Direction.NORTH, PlayerColor.RED);
		run(game(board(space(0, 0)), mover),
				five(CommandInstruction.ENERGY_ROUTINE));

		assertEquals(10, mover.getEnergy());
	}

	@Test
	void repeatRoutineWorksLikeAgain() {
		Player mover = player("ada", 0, 0, Direction.EAST, PlayerColor.RED);
		run(game(board(space(0, 0), space(1, 0), space(2, 0)), mover), List.of(
				CommandInstruction.MOVE_1, CommandInstruction.REPEAT_ROUTINE,
				CommandInstruction.POWER_UP, CommandInstruction.POWER_UP,
				CommandInstruction.POWER_UP));

		assertEquals(new Vector2D(2, 0), mover.getRobot().getCurrentSpace());
	}

	@Test
	void roundRecordsWhoPlayedWhatInActivationOrder() {
		Player near = player("zzz", 1, 0, Direction.EAST, PlayerColor.RED);
		Player far = player("aaa", 3, 0, Direction.EAST, PlayerColor.BLUE);
		Game round = game(board(space(0, 0, new PriorityAntenna()), space(1, 0),
				space(2, 0), space(3, 0), space(4, 0), space(5, 0), space(6, 0),
				space(7, 0)), near, far);
		load(near, List.of(CommandInstruction.POWER_UP,
				CommandInstruction.AGAIN, CommandInstruction.POWER_UP,
				CommandInstruction.POWER_UP, CommandInstruction.POWER_UP));
		load(far, List.of(CommandInstruction.MOVE_2, CommandInstruction.AGAIN,
				CommandInstruction.POWER_UP, CommandInstruction.POWER_UP,
				CommandInstruction.POWER_UP));

		RoundResult result = gameService.activateRound(round);

		assertEquals(5, result.registers().size());
		RegisterReveal first = result.registers().get(0);
		assertEquals(0, first.register());
		assertEquals(List.of("zzz", "aaa"),
				first.cards().stream().map(PlayedCard::username).toList());

		PlayedCard powerUp = first.cards().get(0);
		assertEquals(0, powerUp.stepCount());
		PlayedCard move = first.cards().get(1);
		assertEquals(CommandInstruction.MOVE_2,
				((ProgrammingCard) move.card()).getInstruction());
		assertEquals(0, move.firstStep());
		assertEquals(2, move.stepCount());

		// Again shows as Again, but its frames come from the repeated card
		PlayedCard again = result.registers().get(1).cards().get(1);
		assertEquals(CommandInstruction.AGAIN,
				((ProgrammingCard) again.card()).getInstruction());
		assertEquals(2, again.firstStep());
		assertEquals(2, again.stepCount());
	}

	@Test
	void roundWaitsUntilEveryPlayerSubmitsThenRepublishesTheBoard() {
		InMemoryUserRepository users = new InMemoryUserRepository();
		users.add(new User("August"));
		users.add(new User("Alice"));
		LobbyService lobbies = new LobbyService(new InMemoryLobbyRepository(),
				users, gameService);
		GameLobby lobby = lobbies
				.createLobby(new CreateLobbyDTO("Lobby", "August"));
		lobbies.joinLobby(lobby.getLobbyId(), "Alice");
		lobbies.setReady(lobby.getLobbyId(), "Alice", true);
		lobbies.startGame(lobby.getLobbyId(), "August");

		Game started = gameService.getGame(lobby.getGameId());
		Map<String, Direction> before = facings(started);
		int boardsBefore = boardLobbyIds.size();
		List<CommandInstruction> august = fromHand(named(started, "August"));
		List<CommandInstruction> alice = fromHand(named(started, "Alice"));

		assertFalse(gameService.submitProgram(lobby, "August", august));
		assertEquals(before, facings(started));
		assertEquals(boardsBefore, boardLobbyIds.size());
		assertTrue(roundStartedLobbyIds.isEmpty());
		assertTrue(named(started, "August").getPlayerMat().isSubmitted());
		assertThrows(IllegalStateException.class,
				() -> gameService.submitProgram(lobby, "August", august));

		assertTrue(gameService.submitProgram(lobby, "Alice", alice));
		assertEquals(boardsBefore + 1, boardLobbyIds.size());
		assertEquals(lobby.getLobbyId(),
				boardLobbyIds.get(boardLobbyIds.size() - 1));

		assertEquals(List.of(lobby.getLobbyId()), roundStartedLobbyIds);
		for (Player player : started.getPlayersInGame()) {
			assertFalse(player.getPlayerMat().isSubmitted());
			assertEquals(9, player.getPlayerMat().getHand().size());
			assertEquals(9, player.getPlayerMat().getDiscardPile().size());
			for (Register register : player.getPlayerMat().getRegisters()) {
				assertNull(register.getCard());
			}
		}

		assertFalse(gameService.submitProgram(lobby, "August",
				fromHand(named(started, "August"))));
	}

	private void run(Game round, List<CommandInstruction> registers) {
		for (Player player : round.getPlayersInGame()) {
			load(player, registers);
		}
		gameService.activateRound(round);
	}

	private static void load(Player player,
			List<CommandInstruction> registers) {
		for (int i = 0; i < registers.size(); i++) {
			player.getPlayerMat().getRegisters().get(i)
					.setCard(new ProgrammingCard(registers.get(i)));
		}
	}

	private static List<CommandInstruction> fromHand(Player player) {
		List<CommandInstruction> hand = new ArrayList<>();
		for (ProgrammableCard card : player.getPlayerMat().getHand()) {
			hand.add(((ProgrammingCard) card).getInstruction());
		}
		hand.sort(Comparator.comparing(
				instruction -> instruction == CommandInstruction.AGAIN));
		return hand.subList(0, 5);
	}

	private static Game game(Board board, Player... players) {
		return new Game("round", board, new HashSet<>(List.of(players)),
				Map.of());
	}

	private static Board board(Space... spaces) {
		Map<Vector2D, Space> tiles = new HashMap<>();
		for (Space space : spaces) {
			tiles.put(space.getPosition(), space);
		}
		return new Board(tiles);
	}

	private static Space space(int x, int y, BoardElement... elements) {
		Space space = new Space(new Vector2D(x, y));
		for (BoardElement element : elements) {
			space.addBoardElement(element);
		}
		return space;
	}

	private static Player player(String username, int x, int y,
			Direction facing, PlayerColor color) {
		return new Player(username, new User(username), color,
				new Robot(new Vector2D(x, y), facing));
	}

	private static List<CommandInstruction> five(
			CommandInstruction instruction) {
		return List.of(instruction, instruction, instruction, instruction,
				instruction);
	}

	private static Player named(Game round, String username) {
		return round.getPlayersInGame().stream().filter(
				player -> player.getUser().getUsername().equals(username))
				.findFirst().orElseThrow();
	}

	private static Map<String, Direction> facings(Game round) {
		Map<String, Direction> facings = new HashMap<>();
		for (Player player : round.getPlayersInGame()) {
			facings.put(player.getUser().getUsername(),
					player.getRobot().getOrientation());
		}
		return facings;
	}
}
