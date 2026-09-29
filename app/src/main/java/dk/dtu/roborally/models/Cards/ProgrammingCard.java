package dk.dtu.roborally.models.Cards;

import dk.dtu.roborally.enums.CommandInstruction;
import lombok.Getter;

import java.util.Objects;

/**
 * Represents a programming card in the game, 
 * this holds a beneficial instruction, that the player can use to make up their program
 *
 * @author August
 */

@Getter
public class ProgrammingCard extends ProgrammableCard {
    private final CommandInstruction instruction;

    public ProgrammingCard(CommandInstruction instruction) {
        this.instruction = Objects.requireNonNull(instruction);
    }
}
