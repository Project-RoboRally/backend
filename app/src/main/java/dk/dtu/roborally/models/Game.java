package dk.dtu.roborally.models;

import dk.dtu.roborally.enums.GameState;
import dk.dtu.roborally.enums.GamePhase;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.engine.systems.RegisterSystem;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an active or pending Robo Rally game.
 * Holds the players, board and current game state.
 *
 * @author Elias, Matthias, Victor
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
    private final RegisterSystem registerSystem = new RegisterSystem();

    public Game(String gameID, Board board) {
        this.gameID = gameID;
        this.board = board;
    }

    public boolean addPlayer(Player player) {
        if (gameState != GameState.WAITING || playersInGame.contains(player)) {
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
                if (round.getCurrentRegister() < 4) {
                    round.advanceRegister();
                } else {
                    round.resetRegister();
                    gamePhase = GamePhase.BOARD_ELEMENTS;
                }
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

    public boolean executeCurrentRegister() {
        if (gameState != GameState.RUNNING || gamePhase != GamePhase.EXECUTING_REGISTERS) {
            return false;
        }

        int registerIndex = round.getCurrentRegister();
        for (Player player : playersInGame) {
            registerSystem.executeRegister(player, registerIndex);
        }
        advancePhase();
        return true;
    }

    public boolean movePlayer(Player player, int spaces) {
        if (gameState != GameState.RUNNING
                || !playersInGame.contains(player)
                || player.getRobot() == null
                || !player.getRobot().isActive()
                || spaces <= 0) {
            return false;
        }

        Robot robot = player.getRobot();
        Vector2D destination = getDestination(robot, spaces);
        if (!board.isWithinBoard(destination)) {
            return false;
        }

        robot.setPosition(destination);
        return true;
    }

    private Vector2D getDestination(Robot robot, int spaces) {
        Direction direction = robot.getDirection();
        return switch (direction) {
            case NORTH -> robot.getPosition().add(0, -spaces);
            case EAST -> robot.getPosition().add(spaces, 0);
            case SOUTH -> robot.getPosition().add(0, spaces);
            case WEST -> robot.getPosition().add(-spaces, 0);
        };
    }

    private void startNextRound() {
        round = new Round(round.getNumber() + 1);
        gamePhase = GamePhase.PROGRAMMING;
    }

    public void addPlayerToGame(Player player) {
        playersInGame.add(player);
    }
}