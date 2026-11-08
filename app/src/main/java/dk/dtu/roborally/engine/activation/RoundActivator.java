package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.Checkpoint;
import dk.dtu.roborally.models.board.elements.Wall;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.enums.RegisterStage;
import dk.dtu.roborally.models.game.ActivationLog;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.player.PlayerMat;
import dk.dtu.roborally.models.game.RoundResult;
import dk.dtu.roborally.models.game.RoundResult.PlayedCard;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import dk.dtu.roborally.models.player.RobotView;
import java.util.List;
import java.util.Optional;

/**
 * Runs the activation phase of a round: the programmed registers, and the
 * effects that happen after each register.
 *
 * @author Elias, Nicoleta, Matthias, Sebastian
 */
public class RoundActivator {

	private final GameEventPublisher gameEventPublisher;

	public RoundActivator(GameEventPublisher gameEventPublisher) {
		this.gameEventPublisher = gameEventPublisher;
	}

	/**
	 * Runs the activation phase from wherever it got to and returns everything
	 * that happened in it. Within a register, players go in priority order,
	 * then the end of register effects happen.
	 */
	public RoundResult activate(Game game) {
		Round round = game.getCurrentRound();
		if (game.getPlayersInGame().isEmpty()) {
			return RoundResult.NONE;
		}
		if (!round.isActivating()) {
			round.startActivation();
			startRegister(game, 0);
		}
		while (round.isActivating()) {
			if (game.getGameStatus() == GameStatus.FINISHED) {
				round.finishActivation();
				break;
			}
			step(game);
		}
		return round.getLog().toResult();
	}

	/** Does the next bit of the current register. */
	private void step(Game game) {
		Round round = game.getCurrentRound();
		int register = round.getCurrentRegisterIndex().orElseThrow();
		switch (round.getStage()) {
			case CARDS -> {
				Optional<String> next = round.getNextToActivate();
				if (next.isEmpty()) {
					round.setStage(RegisterStage.END_OF_REGISTER);
					return;
				}
				activateCard(game, findPlayer(game, next.get()), register);
				round.markActivated();
			}
			case END_OF_REGISTER -> {
				evaluateCheckpoints(game, playersInOrder(game));
				if (register + 1 < PlayerMat.REGISTER_COUNT) {
					startRegister(game, register + 1);
				} else {
					round.finishActivation();
				}
			}
		}
	}

	private void startRegister(Game game, int register) {
		game.getCurrentRound().startRegister(register,
				Priority.order(game).stream().map(Player::getId).toList());
	}

	private void activateCard(Game game, Player player, int register) {
		ActivationLog log = game.getCurrentRound().getLog();
		int firstStep = log.stepCount();
		execute(resolveInstruction(player, register), player, game, log);
		log.addCard(register,
				new PlayedCard(player.getUser().getUsername(),
						player.getPlayerMat().getRegisters().get(register)
								.getCard(),
						firstStep, log.stepCount() - firstStep));
	}

	/** Players in the order fixed at the start of this register. */
	private List<Player> playersInOrder(Game game) {
		return game.getCurrentRound().getActivationOrder().stream()
				.map(id -> findPlayer(game, id)).toList();
	}

	private Player findPlayer(Game game, String playerId) {
		for (Player player : game.getPlayersInGame()) {
			if (player.getId().equals(playerId)) {
				return player;
			}
		}
		throw new IllegalStateException(
				"Player is not in this game: " + playerId);
	}

	/**
	 * The instruction a register performs. Again and Repeat Routine repeat the
	 * nearest earlier register that does not repeat anything itself.
	 */
	private CommandInstruction resolveInstruction(Player player, int register) {
		for (int index = register; index >= 0; index--) {
			CommandInstruction instruction = instructionAt(player, index);
			if (!instruction.repeatsPreviousRegister()) {
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
			Game game, ActivationLog log) {
		switch (instruction) {
			case LEFT_TURN -> {
				player.getRobot().turnLeft();
				log.addStep(snapshot(game));
			}
			case RIGHT_TURN -> {
				player.getRobot().turnRight();
				log.addStep(snapshot(game));
			}
			case U_TURN -> {
				player.getRobot().uTurn();
				log.addStep(snapshot(game));
			}
			case MOVE_1, MOVE_2, MOVE_3, SPEED_ROUTINE ->
				move(player, game, instruction.steps(), log);
			case MOVE_BACK -> moveBack(player, game, log);
			case POWER_UP, ENERGY_ROUTINE -> player.addEnergy(1);
			default -> throw new IllegalArgumentException(
					"Cannot activate " + instruction);
		}
	}

	private void move(Player player, Game game, int stepCount,
			ActivationLog log) {
		for (int step = 0; step < stepCount; step++) {
			if (!step(player, game, player.getRobot().getOrientation())) {
				return;
			}
			log.addStep(snapshot(game));
		}
	}

	private void moveBack(Player player, Game game, ActivationLog log) {
		if (step(player, game, player.getRobot().getOrientation().uTurn())) {
			log.addStep(snapshot(game));
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
		if (hasWall(board.getSpace(from), direction)) {
			return true;
		}
		return hasWall(board.getSpace(to), direction.uTurn());
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

	/**
	 * Awards checkpoints in numbered order based on robot positions after a
	 * register. Records the first winner in the supplied player order, finishes
	 * the game, and publishes the updated status.
	 */
	private void evaluateCheckpoints(Game game, List<Player> players) {
		int total = game.getBoard().getElements(Checkpoint.class).size();

		if (total == 0) {
			return;
		}

		for (Player player : players) {
			Vector2D position = player.getRobot().getCurrentSpace();
			Space space = game.getBoard().getSpace(position);

			if (space == null) {
				continue;
			}

			int nextCheckpoint = player.getCheckpointsReached() + 1;

			for (BoardElement element : space.getBoardElements()) {
				if (!(element instanceof Checkpoint checkpoint)) {
					continue;
				}

				if (checkpoint.getNumber() != nextCheckpoint) {
					continue;
				}

				player.reachNextCheckpoint();

				if (player.getCheckpointsReached() == total) {
					game.setWinner(player);
					game.setGameStatus(GameStatus.FINISHED);
					gameEventPublisher.publishStatus(game);
					return;
				}

				break;
			}
		}
	}
}
