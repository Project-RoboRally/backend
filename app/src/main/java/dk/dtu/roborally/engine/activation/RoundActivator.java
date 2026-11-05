package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.Checkpoint;
import dk.dtu.roborally.models.board.elements.Wall;
import dk.dtu.roborally.models.cards.DamageCard;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.enums.RegisterStage;
import dk.dtu.roborally.models.game.ActivationLog;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.player.PlayerMat;
import dk.dtu.roborally.models.player.Register;
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

	/** Virus spreads to every robot this many spaces away or closer. */
	private static final int VIRUS_RANGE = 6;

	private final GameEventPublisher gameEventPublisher;

	public RoundActivator(GameEventPublisher gameEventPublisher) {
		this.gameEventPublisher = gameEventPublisher;
	}

	/**
	 * Runs the activation phase from wherever it got to and returns what
	 * happened since the last call. Within a register, players go in priority
	 * order, then the end of register effects happen. Stops early when a player
	 * has to choose something, the round then says what it is waiting for.
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
		while (round.isActivating() && round.getPendingChoice().isEmpty()) {
			if (game.getGameStatus() == GameStatus.FINISHED) {
				round.finishActivation();
				break;
			}
			step(game);
		}
		return round.takeLog().toResult();
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
				if (activateCard(game, findPlayer(game, next.get()),
						register)) {
					round.markActivated();
				}
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

	/**
	 * Plays the player's card for this register. Returns false when the card
	 * waits on the player choosing an action, nothing has happened yet then.
	 */
	private boolean activateCard(Game game, Player player, int register) {
		Register slot = player.getPlayerMat().getRegisters().get(register);
		while (mustBeReplaced(player, register)) {
			replaceCard(game, player, slot);
		}
		if (slot.getCard() == null) {
			// Nothing left in the deck to replace the card with
			return true;
		}
		CommandInstruction instruction = resolveInstruction(player, register);
		if (!instruction.actionsToChooseFrom().isEmpty()) {
			Optional<CommandInstruction> chosen = chooseAction(game, player,
					instruction);
			if (chosen.isEmpty()) {
				return false;
			}
			instruction = chosen.get();
		}
		ActivationLog log = game.getCurrentRound().getLog();
		int firstStep = log.stepCount();
		execute(instruction, player, game, log);
		log.addCard(register,
				new PlayedCard(player.getUser().getUsername(),
						player.getPlayerMat().getRegisters().get(register)
								.getCard(),
						firstStep, log.stepCount() - firstStep));
		return true;
	}

	/**
	 * Damage cards are swapped for the top card of the deck. So is an Again
	 * with nothing to repeat, which happens when it is drawn into register 1,
	 * or right after a damage card.
	 */
	private boolean mustBeReplaced(Player player, int register) {
		List<Register> registers = player.getPlayerMat().getRegisters();
		ProgrammableCard card = registers.get(register).getCard();
		if (card instanceof DamageCard) {
			return true;
		}
		if (card instanceof ProgrammingCard programming
				&& programming.getInstruction().repeatsPreviousRegister()) {
			return register == 0 || registers.get(register - 1).isDamaged();
		}
		return false;
	}

	/**
	 * Plays out a card that gets replaced. A damage card does its effect and
	 * goes back to its pile, anything else goes to the discard pile.
	 */
	private void replaceCard(Game game, Player player, Register slot) {
		ActivationLog log = game.getCurrentRound().getLog();
		ProgrammableCard card = slot.getCard();
		int firstStep = log.stepCount();
		if (card instanceof DamageCard damage) {
			damageEffect(game, player, damage.getInstruction());
			game.getDamagePiles().giveBack(damage);
			slot.setDamaged(true);
		} else {
			player.getPlayerMat().addToDiscardPile(card);
		}
		log.addCard(slot.getIndex(),
				new PlayedCard(player.getUser().getUsername(), card, firstStep,
						log.stepCount() - firstStep));
		slot.setCard(player.getPlayerMat().drawTop().orElse(null));
	}

	private void damageEffect(Game game, Player player,
			DamageInstruction damage) {
		switch (damage) {
			case TROJAN_HORSE ->
				Damage.take(game, player, DamageInstruction.SPAM, 2);
			case VIRUS -> {
				Vector2D position = player.getRobot().getCurrentSpace();
				for (Player other : game.getPlayersInGame()) {
					if (other != player && other.getRobot().getCurrentSpace()
							.distanceTo(position) <= VIRUS_RANGE) {
						Damage.take(game, other, DamageInstruction.VIRUS, 1);
					}
				}
			}
			case SPAM, WORM -> {
				// SPAM only clogs the deck. Worm reboots, which comes later
			}
		}
	}

	/** Sandbox and Weasel routines let the player pick what they do. */
	private Optional<CommandInstruction> chooseAction(Game game, Player player,
			CommandInstruction routine) {
		ChoiceType type = routine == CommandInstruction.SANDBOX_ROUTINE
				? ChoiceType.SANDBOX_ACTION
				: ChoiceType.WEASEL_ACTION;
		List<String> options = routine.actionsToChooseFrom().stream()
				.map(CommandInstruction::name).toList();
		return game.getCurrentRound()
				.ask(new PendingChoice(player.getId(), type, options))
				.map(CommandInstruction::valueOf);
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
