package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.Checkpoint;
import dk.dtu.roborally.models.board.elements.Pit;
import dk.dtu.roborally.models.cards.DamageCard;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.enums.RegisterStage;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.game.ActivationLog;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.player.PlayerMat;
import dk.dtu.roborally.models.player.Register;
import dk.dtu.roborally.models.game.RoundResult;
import dk.dtu.roborally.models.game.RoundResult.PlayedCard;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.RobotView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Runs the activation phase of a round: the programmed registers, and the
 * effects that happen after each register.
 *
 * @author Elias, Nicoleta, Matthias, Sebastian
 */
public class RoundActivator {

	static final String MOVE_FORWARD = "MOVE_1";
	static final String MOVE_ZERO = "MOVE_0";
	static final String MOVE_LEFT = "LEFT";
	static final String MOVE_RIGHT = "RIGHT";

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
			game.getPlayersInGame()
					.forEach(player -> player.getRobot().clearReboot());
			startRegister(game, 0);
		}
		while (round.isActivating() && round.getPendingChoice().isEmpty()) {
			if (!faceRebootedRobots(game)) {
				break;
			}
			if (game.getGameStatus() == GameStatus.FINISHED) {
				round.finishActivation();
				break;
			}
			step(game);
		}
		return round.takeLog().toResult();
	}

	/**
	 * Asks every rebooted robot's player which way to face. Returns false while
	 * one of them still has to answer.
	 */
	private boolean faceRebootedRobots(Game game) {
		Round round = game.getCurrentRound();
		List<String> options = Arrays.stream(Direction.values())
				.map(Direction::name).toList();
		for (String playerId : round.getRebootsToFace()) {
			Optional<String> facing = round.ask(new PendingChoice(playerId,
					ChoiceType.REBOOT_FACING, options));
			if (facing.isEmpty()) {
				return false;
			}
			findPlayer(game, playerId).getRobot()
					.face(Direction.valueOf(facing.get()));
			round.removeRebootToFace(playerId);
		}
		return true;
	}

	/** Does the next bit of the current register. */
	private void step(Game game) {
		Round round = game.getCurrentRound();
		int register = round.getCurrentRegisterIndex().orElseThrow();
		switch (round.getStage()) {
			case START -> {
				// Sets the order and moves on to the cards once all answered
				RegisterUpgrades.ask(game);
			}
			case CARDS -> {
				Optional<String> next = round.getNextToActivate();
				if (next.isEmpty()) {
					round.setStage(RegisterStage.BOARD_ELEMENTS);
					return;
				}
				if (activateCard(game, findPlayer(game, next.get()),
						register)) {
					round.markActivated();
				}
			}
			case BOARD_ELEMENTS -> {
				BoardElements.activate(game, register, round.getLog());
				round.setStage(RegisterStage.LASERS);
			}
			case LASERS -> {
				if (Lasers.fire(game)) {
					round.setStage(RegisterStage.END_OF_REGISTER);
				}
			}
			case END_OF_REGISTER -> {
				BoardElements.collectEnergy(game, register);
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
		game.getCurrentRound().beginRegister(register);
	}

	/**
	 * Plays the player's card for this register. Returns false when the card
	 * waits on the player choosing an action, nothing has happened yet then.
	 */
	private boolean activateCard(Game game, Player player, int register) {
		if (!TurnUpgrades.ask(game, player)) {
			return false;
		}
		Round round = game.getCurrentRound();
		Register slot = player.getPlayerMat().getRegisters().get(register);
		Optional<CommandInstruction> refreshed = round.getRefreshedTo();
		CommandInstruction instruction;
		ProgrammableCard card;
		if (refreshed.isPresent()) {
			// Refresh swapped the programming for this register
			card = new ProgrammingCard(refreshed.get());
			instruction = refreshed.get().repeatsPreviousRegister()
					? previousInstruction(player, register)
					: refreshed.get();
			if (instruction == null) {
				return true;
			}
		} else {
			while (mustBeReplaced(player, register)) {
				replaceCard(game, player, slot);
			}
			if (slot.getCard() == null) {
				// Nothing left in the deck, or the robot rebooted
				return true;
			}
			// Taken first, a reboot during the move empties the register
			card = slot.getCard();
			instruction = resolveInstruction(player, register);
		}
		if (!instruction.actionsToChooseFrom().isEmpty()) {
			Optional<CommandInstruction> chosen = chooseAction(game, player,
					instruction);
			if (chosen.isEmpty()) {
				return false;
			}
			instruction = chosen.get();
		}
		Direction pushTo = null;
		if (player.getUpgrades().has(UpgradeType.SIDE_ARMS)
				&& robotWithinReach(game, player, instruction)) {
			Optional<String> side = askThisTurn(round,
					new PendingChoice(player.getId(), ChoiceType.SIDE_ARMS,
							List.of("FORWARD", "LEFT", "RIGHT")));
			if (side.isEmpty()) {
				return false;
			}
			Direction facing = player.getRobot().getOrientation();
			pushTo = switch (side.get()) {
				case "LEFT" -> facing.turnLeft();
				case "RIGHT" -> facing.turnRight();
				default -> null;
			};
		}
		round.setSidePush(pushTo);
		boolean teleport = false;
		if (teleportTarget(game, player, instruction).isPresent()) {
			Optional<String> answer = askThisTurn(round, new PendingChoice(
					player.getId(), ChoiceType.TELEPORT, List.of("NO", "YES")));
			if (answer.isEmpty()) {
				return false;
			}
			teleport = answer.get().equals("YES");
		}
		String moveOne = null;
		if (!teleport && instruction == CommandInstruction.MOVE_1) {
			List<String> options = moveOneOptions(player);
			if (options.size() > 1) {
				Optional<String> how = askThisTurn(round, new PendingChoice(
						player.getId(), ChoiceType.MOVE_ONE, options));
				if (how.isEmpty()) {
					return false;
				}
				moveOne = how.get();
			}
		}
		ActivationLog log = round.getLog();
		int firstStep = log.stepCount();
		int times = round.isHacked() ? 2 : 1;
		for (int i = 0; i < times; i++) {
			if (i > 0 && player.getRobot().isRebootedThisRound()) {
				// Fell off the first time, the second one does not happen
				break;
			}
			if (teleport && player.getEnergy() >= 1
					&& teleportTarget(game, player, instruction).isPresent()) {
				teleport(game, player,
						teleportTarget(game, player, instruction).get(), log);
			} else if (moveOne == null) {
				execute(instruction, player, game, log);
			} else {
				moveOne(player, game, moveOne, log);
			}
		}
		log.addCard(register, new PlayedCard(player.getUser().getUsername(),
				card, firstStep, log.stepCount() - firstStep));
		return true;
	}

	/**
	 * Where a Teleporter would take the robot for this move card, if it has one
	 * and the energy to pay. It ignores walls, pits, the antenna and robots on
	 * the way, but can not end on a pit, the antenna or off the board.
	 */
	private Optional<Vector2D> teleportTarget(Game game, Player player,
			CommandInstruction instruction) {
		if (!player.getUpgrades().has(UpgradeType.TELEPORTER)
				|| player.getEnergy() < 1) {
			return Optional.empty();
		}
		Direction facing = player.getRobot().getOrientation();
		Vector2D target = switch (instruction) {
			case MOVE_1, MOVE_2, MOVE_3, SPEED_ROUTINE -> {
				Vector2D step = facing.step();
				int steps = instruction.steps();
				yield player.getRobot().getCurrentSpace().add(step.x() * steps,
						step.y() * steps);
			}
			case MOVE_BACK ->
				player.getRobot().getCurrentSpace().add(facing.uTurn().step());
			default -> null;
		};
		if (target == null || !game.getBoard().isWithinBoard(target)) {
			return Optional.empty();
		}
		Space space = game.getBoard().getSpace(target);
		if (Movement.hasAntenna(space) || space.getBoardElements().stream()
				.anyMatch(Pit.class::isInstance)) {
			return Optional.empty();
		}
		return Optional.of(target);
	}

	/** Pays 1 energy and jumps, swapping places with a robot already there. */
	private void teleport(Game game, Player player, Vector2D target,
			ActivationLog log) {
		player.spendEnergy(1);
		Vector2D from = player.getRobot().getCurrentSpace();
		Movement.robotAt(game, target).filter(other -> other != player)
				.ifPresent(other -> other.getRobot().moveTo(from));
		player.getRobot().moveTo(target);
		log.addStep(snapshot(game));
	}

	/**
	 * Whether a move card could push a robot, so Side Arms is worth asking
	 * about: a robot within the move's reach, with no wall before it.
	 */
	private boolean robotWithinReach(Game game, Player player,
			CommandInstruction instruction) {
		Direction facing = player.getRobot().getOrientation();
		int reach;
		Direction direction = facing;
		switch (instruction) {
			case MOVE_1, MOVE_2, MOVE_3, SPEED_ROUTINE ->
				reach = instruction.steps();
			case MOVE_BACK -> {
				reach = 1;
				direction = facing.uTurn();
			}
			default -> {
				return false;
			}
		}
		Vector2D position = player.getRobot().getCurrentSpace();
		for (int i = 0; i < reach; i++) {
			Vector2D next = position.add(direction.step());
			if (!game.getBoard().isWithinBoard(next) || Movement
					.wallBlocks(game.getBoard(), position, next, direction)) {
				return false;
			}
			if (Movement.robotAt(game, next).isPresent()) {
				return true;
			}
			position = next;
		}
		return false;
	}

	/**
	 * How a Move 1 can be played. Brakes make it a Move 0, Crab Legs move one
	 * space left or right without turning.
	 */
	private List<String> moveOneOptions(Player player) {
		List<String> options = new ArrayList<>();
		options.add(MOVE_FORWARD);
		if (player.getUpgrades().has(UpgradeType.BRAKES)) {
			options.add(MOVE_ZERO);
		}
		if (player.getUpgrades().has(UpgradeType.CRAB_LEGS)) {
			options.add(MOVE_LEFT);
			options.add(MOVE_RIGHT);
		}
		return options;
	}

	private void moveOne(Player player, Game game, String how,
			ActivationLog log) {
		Direction facing = player.getRobot().getOrientation();
		switch (how) {
			case MOVE_ZERO -> {
				// Brakes on, the robot stays put
			}
			case MOVE_LEFT ->
				moveSteps(player, game, facing.turnLeft(), 1, log);
			case MOVE_RIGHT ->
				moveSteps(player, game, facing.turnRight(), 1, log);
			default -> moveSteps(player, game, facing, 1, log);
		}
	}

	/**
	 * Asks once per turn. The answer is kept for the rest of the turn, so a
	 * card played twice by Hack does not ask again.
	 */
	private Optional<String> askThisTurn(Round round, PendingChoice question) {
		Optional<String> kept = round.getTurnAnswer(question.type());
		if (kept.isPresent()) {
			return kept;
		}
		Optional<String> answer = round.ask(question);
		answer.ifPresent(given -> round.setTurnAnswer(question.type(), given));
		return answer;
	}

	/** What an Again from Refresh repeats, null in register 1. */
	private CommandInstruction previousInstruction(Player player,
			int register) {
		return register == 0 ? null : resolveInstruction(player, register - 1);
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
		slot.setCard(null);
		int firstStep = log.stepCount();
		boolean rebootedBefore = player.getRobot().isRebootedThisRound();
		if (card instanceof DamageCard damage) {
			game.getDamagePiles().giveBack(damage);
			slot.setDamaged(true);
			damageEffect(game, player, damage.getInstruction());
		} else {
			player.getPlayerMat().addToDiscardPile(card);
		}
		log.addCard(slot.getIndex(),
				new PlayedCard(player.getUser().getUsername(), card, firstStep,
						log.stepCount() - firstStep));
		if (player.getRobot().isRebootedThisRound() && !rebootedBefore) {
			// A worm rebooted the robot, its program is cancelled
			return;
		}
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
			case WORM -> Reboot.reboot(game, player, true,
					player.getRobot().getCurrentSpace());
			case SPAM -> {
				// SPAM only clogs up the deck
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
			case SPAM_FOLDER -> player.getPlayerMat()
					.removeFromDiscardPile(DamageInstruction.SPAM.name())
					.ifPresent(spam -> game.getDamagePiles()
							.giveBack((DamageCard) spam));
			default -> throw new IllegalArgumentException(
					"Cannot activate " + instruction);
		}
	}

	private void move(Player player, Game game, int stepCount,
			ActivationLog log) {
		moveSteps(player, game, player.getRobot().getOrientation(), stepCount,
				log);
	}

	/**
	 * Moves the robot space by space. With a Hover Unit it passes over pits and
	 * only falls in if the move ends on one.
	 */
	private void moveSteps(Player player, Game game, Direction direction,
			int stepCount, ActivationLog log) {
		boolean hovering = player.getUpgrades().has(UpgradeType.HOVER_UNIT);
		// One push event per robot, however far this card pushes it
		Set<Player> pushed = new LinkedHashSet<>();
		Direction pushTo = game.getCurrentRound().getSidePush()
				.orElse(direction);
		for (int step = 0; step < stepCount; step++) {
			if (!Movement.step(game, player, direction, hovering, pushTo,
					pushed::add)) {
				break;
			}
			log.addStep(snapshot(game));
		}
		if (hovering) {
			Movement.landOnPit(game, player);
		}
		pushed.forEach(target -> PushUpgrades.pushed(game, player, target));
	}

	private void moveBack(Player player, Game game, ActivationLog log) {
		moveSteps(player, game, player.getRobot().getOrientation().uTurn(), 1,
				log);
	}

	private List<RobotView> snapshot(Game game) {
		return game.getPlayersInGame().stream().map(RobotView::from).toList();
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
