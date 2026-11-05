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
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.RoundResult;
import dk.dtu.roborally.models.game.RoundResult.PlayedCard;
import dk.dtu.roborally.models.game.RoundResult.RegisterReveal;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import dk.dtu.roborally.models.player.RobotView;
import java.util.ArrayList;
import java.util.List;

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
	 * Runs registers 1 through 5. Within a register, the robot closest to the
	 * priority antenna moves first, with username breaking ties.
	 */
	public RoundResult activate(Game game) {
		List<List<RobotView>> steps = new ArrayList<>();
		List<RegisterReveal> reveals = new ArrayList<>();
		List<Player> players = new ArrayList<>(game.getPlayersInGame());
		if (players.isEmpty()) {
			return RoundResult.NONE;
		}
		int registerCount = players.get(0).getPlayerMat().getRegisters().size();

		for (int register = 0; register < registerCount; register++) {
			List<Player> order = Priority.order(game);

			List<PlayedCard> cards = new ArrayList<>();
			for (Player player : order) {
				int firstStep = steps.size();
				execute(resolveInstruction(player, register), player, game,
						steps);
				cards.add(new PlayedCard(player.getUser().getUsername(),
						player.getPlayerMat().getRegisters().get(register)
								.getCard(),
						firstStep, steps.size() - firstStep));
			}
			reveals.add(new RegisterReveal(register, cards));
			evaluateCheckpoints(game, order);

			if (game.getGameStatus() == GameStatus.FINISHED) {
				break;
			}
		}
		return new RoundResult(steps, reveals);
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
			case MOVE_1, MOVE_2, MOVE_3, SPEED_ROUTINE ->
				move(player, game, instruction.steps(), steps);
			case MOVE_BACK -> moveBack(player, game, steps);
			case POWER_UP, ENERGY_ROUTINE -> player.addEnergy(1);
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
