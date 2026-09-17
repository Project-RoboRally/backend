package dk.dtu.roborally.models;

import lombok.Getter;

/**
 * Represents the state of the current game round.
 *
 * @author Sebastian
 */
public class Round {

    @Getter
    private int number;

    @Getter
    private int currentRegister;

    public Round(int number) {
        this.number = number;
        this.currentRegister = 0;
    }

    public void advanceRegister() {
        // Registers are indexed from 0 to 4, so index 4 is the fifth and final register.
        if (currentRegister < 4) {
            currentRegister++;
        }
    }

    public void resetRegister() {
        currentRegister = 0;
    }
}
