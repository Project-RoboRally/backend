package dk.dtu.roborally.models.game;

import dk.dtu.roborally.engine.systems.RegisterSystem;
import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.GamePhase;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.cards.CardDeck;
import dk.dtu.roborally.models.cards.DamageCard;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents an active or pending Robo Rally game. Holds the players, board,
 * shared game cards (like damage cards) and current game status.
 *
 * @author Elias, Matthias, August, Sebastian, Victor
 */
public class Game {

	@Getter
	private final String gameID;
	@Getter
	private final Set<Player> playersInGame = new HashSet<>();
	@Getter
	@Setter
	private GameStatus gameStatus = GameStatus.IN_PROGRESS;
	@Getter
	@Setter
	private GamePhase gamePhase;
	@Getter
	private final Board board;
	@Getter
	private Round currentRound = new Round();
	private final Map<DamageInstruction, CardDeck<DamageCard>> damageSupplies;
	private final RegisterSystem registerSystem = new RegisterSystem();

	public Game(String gameID, Board board,
			Map<DamageInstruction, CardDeck<DamageCard>> damageSupplies) {
		this.gameID = gameID;
		this.board = board;
		this.damageSupplies = damageSupplies;
	}

	public boolean addPlayer(Player player) {
		if (gamePhase != null || playersInGame.contains(player)) {
			return false;
		}

		playersInGame.add(player);
		return true;
	}

	public boolean removePlayer(Player player) {
		if (!playersInGame.contains(player)) {
			return false;
		}

		playersInGame.remove(player);
		return true;
	}

	public boolean start() {
		if (gamePhase != null || gameStatus == GameStatus.FINISHED) {
			return false;
		}

		currentRound = new Round(1);
		gamePhase = GamePhase.PROGRAMMING;
		return true;
	}

	public boolean advancePhase() {
		if (gameStatus == GameStatus.FINISHED || gamePhase == null) {
			return false;
		}

		switch (gamePhase) {
			case PROGRAMMING :
				gamePhase = GamePhase.EXECUTING_REGISTERS;
				break;
			case EXECUTING_REGISTERS :
				if (currentRound.getCurrentRegister() < 4) {
					currentRound.advanceRegister();
				} else {
					currentRound.resetRegister();
					gamePhase = GamePhase.BOARD_ELEMENTS;
				}
				break;
			case BOARD_ELEMENTS :
				gamePhase = GamePhase.LASERS;
				break;
			case LASERS :
				gamePhase = GamePhase.ROUND_END;
				break;
			case ROUND_END :
				startNextRound();
				break;
		}

		return true;
	}

	public boolean executeCurrentRegister() {
		if (gameStatus == GameStatus.FINISHED
				|| gamePhase != GamePhase.EXECUTING_REGISTERS) {
			return false;
		}

		int registerIndex = currentRound.getCurrentRegister();
		for (Player player : playersInGame) {
			registerSystem.executeRegister(player, registerIndex);
		}
		advancePhase();
		return true;
	}

	public boolean movePlayer(Player player, int spaces) {
		if (gameStatus == GameStatus.FINISHED || !playersInGame.contains(player)
				|| player.getRobot() == null || !player.getRobot().isActive()
				|| spaces <= 0) {
			return false;
		}

		Robot robot = player.getRobot();
		Vector2D destination = getDestination(robot, spaces);
		if (!board.isWithinBoard(destination)) {
			return false;
		}

		robot.setCurrentSpace(destination);
		return true;
	}

	private Vector2D getDestination(Robot robot, int spaces) {
		Direction direction = robot.getOrientation();
		return switch (direction) {
			case NORTH -> robot.getCurrentSpace().add(0, -spaces);
			case EAST -> robot.getCurrentSpace().add(spaces, 0);
			case SOUTH -> robot.getCurrentSpace().add(0, spaces);
			case WEST -> robot.getCurrentSpace().add(-spaces, 0);
		};
	}

	private void startNextRound() {
		currentRound = new Round(currentRound.getNumber() + 1);
		gamePhase = GamePhase.PROGRAMMING;
	}

	public void addPlayerToGame(Player player) {
		playersInGame.add(player);
	}
}
