package dk.dtu.roborally.models;

import dk.dtu.roborally.enums.RotationDirection;
import lombok.Getter;

/**
 * Represents a rotating gear placed on a board tile.
 * A gear rotates robots standing on it.
 *
 * @author Matthias
 */
public class Gear extends BoardElement {

    @Getter
    private final RotationDirection rotationDirection;

    public Gear(RotationDirection rotationDirection) {
        this.rotationDirection = rotationDirection;
    }
}