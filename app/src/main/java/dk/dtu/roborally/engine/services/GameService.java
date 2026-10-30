package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.exceptions.game.GameNotFoundException;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.PriorityAntenna;
import dk.dtu.roborally.models.board.elements.Wall;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import dk.dtu.roborally.models.player.RobotView;
import dk.dtu.roborally.repository.GameRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service for logic about a game
 *
 * @author Elias, Nicoleta, Sebastian
 */
public class GameService {

	private final GameRepository gameRepository;
	private final GameEventPublisher gameEventPublisher;

	public GameService(GameRepository gameRepository,
			GameEventPublisher gameEventPublisher) {
		this.gameRepository = gameRepository;
		this.gameEventPublisher = gameEventPublisher;
	}

	public Game getGame(String gameId) {
		return gameRepository.getById(gameId)
				.orElseThrow(() -> new GameNotFoundException(gameId));
	}

	public GameStatus getStatus(String gameId) {
		return getGame(gameId).getGameStatus();
	}

	/**
	 * Changes the status of a game and notifies subscribers. Does nothing if
	 * the game already has the given status.
	 */
	public Game updateStatus(String gameId, GameStatus newStatus) {
		Game game = getGame(gameId);

		if (game.getGameStatus() == newStatus) {
			return game;
		}

		game.setGameStatus(newStatus);
		gameEventPublisher.publishStatus(game);
		return game;
	}

	public Game finishGame(String gameId) {
		return updateStatus(gameId, GameStatus.FINISHED);
	}

	/**
	 * Registers a game that a lobby has just created and notifies the lobby's
	 * subscribers. The board message includes each robot on its starting space.
	 * Validation already happened in GameLobby.startGame().
	 */
	public void startGame(String lobbyId, Game game) {
		gameRepository.add(game);
		gameEventPublisher.publishBoard(lobbyId, game);
		gameEventPublisher.publishGameStarted(lobbyId, game);
	}

	/**
	 * Stores this player's five-card program. When every player has submitted,
	 * resolves the round, clears the mats, draws new hands, republishes the
	 * board, and returns true. Returns false while the round is still waiting.
	 */
	public boolean submitProgram(GameLobby lobby, String username,
			List<CommandInstruction> registers) {
		if (!lobby.isGameStarted()) {
			throw new IllegalStateException("The game has not been started.");
		}

		Game game = getGame(lobby.getGameId());
		findPlayer(game, username).getPlayerMat().program(registers);

		boolean everyoneSubmitted = game.getPlayersInGame().stream()
				.allMatch(player -> player.getPlayerMat().isSubmitted());
		if (!everyoneSubmitted) {
			return false;
		}

		List<List<RobotView>> steps = activateRound(game);
		for (Player player : game.getPlayersInGame()) {
			player.getPlayerMat().clearProgram();
			player.getPlayerMat().drawHand();
		}
		gameEventPublisher.publishRound(lobby.getLobbyId(), game, steps);
		return true;
	}

	/**
	 * Runs registers 1 through 5. Within a register, the robot closest to the
	 * priority antenna moves first, with username breaking ties.
	 */
	List<List<RobotView>> activateRound(Game game) {
		List<List<RobotView>> steps = new ArrayList<>();
		Vector2D antenna = antennaPosition(game.getBoard());
		List<Player> players = new ArrayList<>(game.getPlayersInGame());
		if (players.isEmpty()) {
			return steps;
		}
		int registerCount = players.get(0).getPlayerMat().getRegisters().size();

		for (int register = 0; register < registerCount; register++) {
			List<Player> order = new ArrayList<>(players);
			order.sort(activationOrder(antenna));

			for (Player player : order) {
				execute(resolveInstruction(player, register), player, game,
						steps);
			}
		}
		return steps;
	}

	private Comparator<Player> activationOrder(Vector2D antenna) {
		return Comparator
				.comparingInt(
						(Player player) -> distanceToAntenna(player, antenna))
				.thenComparing(player -> player.getUser().getUsername());
	}

	private Player findPlayer(Game game, String username) {
		for (Player player : game.getPlayersInGame()) {
			if (player.getUser().getUsername().equals(username)) {
				return player;
			}
		}
		throw new IllegalArgumentException(
				"Player is not in this game: " + username);
	}

	/**
	 * The instruction a register performs. Again repeats the nearest earlier
	 * register that is not itself Again.
	 */
	private CommandInstruction resolveInstruction(Player player, int register) {
		for (int index = register; index >= 0; index--) {
			CommandInstruction instruction = instructionAt(player, index);
			if (instruction != CommandInstruction.AGAIN) {
				return instruction;
			}
		}
		throw new IllegalStateException("Again has no register to repeat");
	}

	private CommandInstruction instructionAt(Player player, int register) {
		ProgrammableCard card = player.getPlayerMat().getRegisters()
				.get(register).getCard();
		if (card instanceof ProgrammingCard programmingCard) {
			return programmingCard.getInstruction();
		}
		throw new IllegalStateException(
				"Register " + (register + 1) + " has no programming card");
	}

	private void execute(CommandInstruction instruction, Player player,
			Game game, List<List<RobotView>> steps) {
		switch (instruction) {
			case LEFT_TURN -> {
				player.getRobot().turnLeft();
				steps.add(snapshot(game));
			}
			case RIGHT_TURN -> {
				player.getRobot().turnRight();
				steps.add(snapshot(game));
			}
			case U_TURN -> {
				player.getRobot().uTurn();
				steps.add(snapshot(game));
			}
			case MOVE_1, MOVE_2, MOVE_3 ->
				move(player, game, instruction.steps(), steps);
			case MOVE_BACK -> moveBack(player, game, steps);
			case POWER_UP -> player.addEnergy(1);
			default -> throw new IllegalArgumentException(
					"Cannot activate " + instruction);
		}
	}

	private void move(Player player, Game game, int stepCount,
			List<List<RobotView>> steps) {
		for (int step = 0; step < stepCount; step++) {
			if (!step(player, game, player.getRobot().getOrientation())) {
				return;
			}
			steps.add(snapshot(game));
		}
	}

	private void moveBack(Player player, Game game,
			List<List<RobotView>> steps) {
		if (step(player, game, player.getRobot().getOrientation().uTurn())) {
			steps.add(snapshot(game));
		}
	}

	private List<RobotView> snapshot(Game game) {
		return game.getPlayersInGame().stream().map(RobotView::from).toList();
	}

	/**
	 * One space along the given direction. Off the board, a wall on the crossed
	 * edge, or another robot blocks the step.
	 */
	private boolean step(Player player, Game game, Direction direction) {
		Robot robot = player.getRobot();
		Vector2D from = robot.getCurrentSpace();
		Vector2D to = from.add(direction.step());
		Board board = game.getBoard();

		if (!board.isWithinBoard(to) || wallBlocks(board, from, to, direction)
				|| occupied(game, to, player)) {
			return false;
		}

		robot.moveTo(to);
		return true;
	}

	private boolean wallBlocks(Board board, Vector2D from, Vector2D to,
			Direction direction) {
		if (hasWall(board.getSpace(from.x(), from.y()), direction)) {
			return true;
		}
		return hasWall(board.getSpace(to.x(), to.y()), direction.uTurn());
	}

	private boolean hasWall(Space space, Direction side) {
		if (space == null) {
			return false;
		}
		for (BoardElement element : space.getBoardElements()) {
			if (element instanceof Wall wall && wall.getSide() == side) {
				return true;
			}
		}
		return false;
	}

	private boolean occupied(Game game, Vector2D position, Player moving) {
		for (Player other : game.getPlayersInGame()) {
			if (other != moving
					&& position.equals(other.getRobot().getCurrentSpace())) {
				return true;
			}
		}
		return false;
	}

	private Vector2D antennaPosition(Board board) {
		for (Space space : board.getSpaceMap().values()) {
			for (BoardElement element : space.getBoardElements()) {
				if (element instanceof PriorityAntenna) {
					return space.getPosition();
				}
			}
		}
		return null;
	}

	/**
	 * Manhattan distance to the antenna. With no antenna on the board every
	 * robot is equally close, so username decides the order.
	 */
	private int distanceToAntenna(Player player, Vector2D antenna) {
		if (antenna == null) {
			return 0;
		}
		Vector2D position = player.getRobot().getCurrentSpace();
		return Math.abs(position.x() - antenna.x())
				+ Math.abs(position.y() - antenna.y());
	}
}
