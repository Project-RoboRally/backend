package dk.dtu.roborally.objects;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

public class Player {

    @Nullable @Getter @Setter
    private Robot robot;

    @Getter
    private final int playerID;

    @Getter
    private PlayerMat playerMat;

    public Player(int playerID) {
        this.playerID = playerID;
    }
}
