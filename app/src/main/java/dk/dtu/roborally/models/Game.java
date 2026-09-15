package dk.dtu.roborally.models;

import dk.dtu.roborally.enums.GameState;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an active or pending Robo Rally game.
 * Holds the players, board and current game state.
 *
 * @author Elias, Matthias
 */
public class Game {

    @Getter
    private final String gameID;
    @Getter
    private final Set<Player> playersInGame = new HashSet<>();
    @Getter
    @Setter
    private GameState gameState = GameState.WAITING;
    @Getter
    private final Board board;

    public Game(String gameID, Board board) {
        this.gameID = gameID;
        this.board = board;
    }

    public boolean start() {
        return true;
    }

    public void addPlayerToGame(Player player) {
        playersInGame.add(player);
    }
}