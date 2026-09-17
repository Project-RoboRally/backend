package dk.dtu.roborally.models;

import dk.dtu.roborally.enums.GameState;
import dk.dtu.roborally.enums.GamePhase;
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
    @Setter
    private GamePhase gamePhase;
    @Getter
    private Round round;
    @Getter
    private final Board board;

    public Game(String gameID, Board board) {
        this.gameID = gameID;
        this.board = board;
    }

    public boolean start() {
        if (gameState != GameState.WAITING) {
            return false;
        }

        gameState = GameState.RUNNING;
        round = new Round(1);
        gamePhase = GamePhase.PROGRAMMING;
        return true;
    }

    public boolean advancePhase() {
        if (gameState != GameState.RUNNING || gamePhase == null) {
            return false;
        }

        switch (gamePhase) {
            case PROGRAMMING:
                gamePhase = GamePhase.EXECUTING_REGISTERS;
                break;
            case EXECUTING_REGISTERS:
                gamePhase = GamePhase.BOARD_ELEMENTS;
                break;
            case BOARD_ELEMENTS:
                gamePhase = GamePhase.LASERS;
                break;
            case LASERS:
                gamePhase = GamePhase.ROUND_END;
                break;
            case ROUND_END:
                startNextRound();
                break;
        }

        return true;
    }

    private void startNextRound() {
        round = new Round(round.getNumber() + 1);
        gamePhase = GamePhase.PROGRAMMING;
    }

    public void addPlayerToGame(Player player) {
        playersInGame.add(player);
    }
}