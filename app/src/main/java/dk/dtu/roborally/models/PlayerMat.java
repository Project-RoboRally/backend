package dk.dtu.roborally.models;

import lombok.Getter;

/**
 * Represents the individual player mat used to store programming registers
 * and player-specific robot state.
 *
 * @author Matthias
 */
public class PlayerMat {

    @Getter
    private final Register[] registers = new Register[5];

    public PlayerMat() {
        for (int i = 0; i < registers.length; i++) {
            registers[i] = new Register(i);
        }
    }
}