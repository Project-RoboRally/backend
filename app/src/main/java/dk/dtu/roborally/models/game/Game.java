package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.player.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Represents an active or pending Robo Rally game. Holds the players, board,
 * shared game cards (like damage cards) and current game status
 *
 * @author Elias, Matthias, August, Sebastian
 */
public class Game {

	@Getter
	private final String gameID;
	@Getter
	private final Set<Player> playersInGame;
	@Getter
	@Setter
	private GameStatus gameStatus = GameStatus.IN_PROGRESS;
	@Getter
	@Setter
	private Player winner;
	@Getter
	private final Board board;
	@Getter
	private Round currentRound = new Round();
	@Getter
	private final UpgradeShop upgradeShop = new UpgradeShop();
	@Getter
	private final DamagePiles damagePiles = new DamagePiles();
	private final Deque<Player> upgradeOrder = new ArrayDeque<>();

	public Game(String gameID, Board board, Set<Player> players) {
		this.gameID = gameID;
		this.board = board;
		this.playersInGame = players;
	}

	/** Sets who gets to buy an upgrade this round, first buyer first. */
	public void setUpgradeOrder(List<Player> players) {
		upgradeOrder.clear();
		upgradeOrder.addAll(players);
	}

	/** Buyers still waiting, current buyer first. */
	public List<Player> getUpgradeOrder() {
		return List.copyOf(upgradeOrder);
	}

	/** The player whose turn it is to buy, if the upgrade phase is running. */
	public Optional<Player> getCurrentBuyer() {
		return Optional.ofNullable(upgradeOrder.peekFirst());
	}

	public void endUpgradeTurn() {
		upgradeOrder.pollFirst();
	}
}
