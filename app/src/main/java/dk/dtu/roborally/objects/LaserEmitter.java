package dk.dtu.roborally.objects;

import dk.dtu.roborally.enums.Direction;
import lombok.Getter;

/**
 * Represents a laser emitter placed on the game board. A laser emitter fires in
 * a specified direction and can damage robots.
 *
 * NOTE: Laser.java is left open in case we want to do a class for the "bullet".
 *
 * @author Matthias
 */
public class LaserEmitter extends BoardElement {

	@Getter
	private final Direction direction;

	@Getter
	private final int damage;

	public LaserEmitter(Direction direction, int damage) {
		this.direction = direction;
		this.damage = damage;
	}
}
