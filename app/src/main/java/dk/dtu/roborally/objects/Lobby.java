package dk.dtu.roborally.objects;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Server needs to hold n lobbies, and every lobby can hold a singular game.
 *
 * @author Victor, Sebastian
 */

public class Lobby {
    @Getter
    private final Set<Player> playersInLobby = new HashSet<>();

    @Getter
    @Setter
    private Game game;

    public Lobby() {

    }

    public void addPlayerToLobby(Player player) {
        playersInLobby.add(player);
    }

    public void startGame(Game game) {
        game.start();
    }
}
