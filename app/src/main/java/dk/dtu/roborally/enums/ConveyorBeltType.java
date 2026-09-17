package dk.dtu.roborally.enums;

/**
 * Definitions of the two types of Conveyor belts: Green and Blue.
 *
 * @author Matthias
 */

public enum ConveyorBeltType {
	GREEN(1), BLUE(2);

	private final int speed;

	ConveyorBeltType(int speed) {
		this.speed = speed;
	}

	public int getSpeed() {
		return speed;
	}
}
