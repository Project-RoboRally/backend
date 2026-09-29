package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.cards.CardDeck;
import dk.dtu.roborally.models.cards.DamageCard;
import dk.dtu.roborally.models.player.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents an active or pending Robo Rally game. Holds the players, board,
 * shared game cards (like damage cards) and current game status
 *
 * @author Elias, Matthias, August
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
	private final Board board;
	@Getter
	private Round currentRound = new Round();
	private final Map<DamageInstruction, CardDeck<DamageCard>> damageSupplies;

	public Game(String gameID, Board board,
			Map<DamageInstruction, CardDeck<DamageCard>> damageSupplies) {
		this.gameID = gameID;
		this.board = board;
		this.damageSupplies = damageSupplies;
	}

	public boolean start() {
		return true;
	}

	public void addPlayerToGame(Player player) {
		playersInGame.add(player);
	}
}
