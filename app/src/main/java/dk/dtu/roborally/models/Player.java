package dk.dtu.roborally.models;

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
    private final String username;

    @Getter
    private final PlayerMat playerMat;

    public Player(String username) {
        this.username = username;
        this.playerMat = new PlayerMat();
    }
}