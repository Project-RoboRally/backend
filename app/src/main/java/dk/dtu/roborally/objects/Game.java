package dk.dtu.roborally.objects;

import dk.dtu.roborally.enums.GameState;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

public class Game {

    @Getter @Setter
    private GameState gameState = GameState.WAITING;

    @Getter
    private Set<Player> playersInGame = new HashSet<>();

    @Getter
    private Board board;

    @Getter
    private Timer timer;


    public void addPlayerToGame(Player player) {
//        if (isPlayerInGame(player))
//            throw new RuntimeException("Player is already in this game.");
//
//        if (gameState == GameState.RUNNING)
//            throw new RuntimeException("Game is running. No new player can be added at this time.");

        playersInGame.add(player);
    }

//    public boolean isPlayerInGame(Player player) {
//        return isPlayerInGame(player.getPlayerID());
//    }
//
//    public boolean isPlayerInGame(int playerID) {
//        for (Player player : playersInGame) {
//            if (player.getPlayerID() == playerID)
//                return true;
//        }
//        return false;
//    }
}
