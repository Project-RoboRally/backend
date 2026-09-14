package dk.dtu.roborally.objects;

import dk.dtu.roborally.enums.GameState;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an active or pending Robo Rally game. Holds the players, board and
 * current game state.
 *
 * @author Elias, Matthias
 */
public class Game {

	@Getter
	@Setter
	private GameState gameState = GameState.WAITING;

	@Getter
	private final Set<Player> playersInGame = new HashSet<>();

	@Getter
	private Board board;

	public Game(Board board) {
		this.board = board;
	}

	public void addPlayerToGame(Player player) {
		playersInGame.add(player);
	}
}
