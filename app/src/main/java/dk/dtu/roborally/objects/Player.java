package dk.dtu.roborally.objects;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * Represents a player participating in a Robo Rally game.
 *
 * @author Elias, Matthias
 */
public class Player {

    @Nullable
    @Getter @Setter
    private Robot robot;

    @Getter
    private final int playerID;

    @Getter
    private final PlayerMat playerMat;

    public Player(int playerID) {
        this.playerID = playerID;
        this.playerMat = new PlayerMat();
    }
}