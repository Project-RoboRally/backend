package dk.dtu.roborally.models.player;

import java.util.List;
import java.util.Map;

import dk.dtu.roborally.enums.ConveyorBeltType;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.Checkpoint;
import dk.dtu.roborally.models.board.elements.ConveyorBelt;
import dk.dtu.roborally.models.board.elements.EnergySpace;
import dk.dtu.roborally.models.board.elements.Gear;
import dk.dtu.roborally.models.board.elements.PushPanel;
import lombok.Getter;

/**
 * Represents a player's robot and its current state on the board.
 *
 * @author Elias, Matthias, August, Victor
 */
public class Robot {

	@Getter
	private Vector2D currentSpace;

	@Getter
	private Direction orientation;

	@Getter
	private boolean rebootedThisRound;

	public Robot(Vector2D space, Direction orientation) {
		this.currentSpace = space;
		this.orientation = orientation;
	}

	public void turnRight() {
		orientation = orientation.turnRight();
	}

	public void turnLeft() {
		orientation = orientation.turnLeft();
	}

	public void uTurn() {
		orientation = orientation.uTurn();
	}

	public void moveTo(Vector2D space) {
		currentSpace = space;
	}

	public void conveyorRotate(ConveyorBelt conveyor) {
		switch (conveyor.getType()) {
			case BLUE_RIGHT_CURVE:
			case GREEN_RIGHT_CURVE:
				turnRight();
				break;
			case BLUE_LEFT_CURVE:
			case GREEN_LEFT_CURVE:
				turnLeft();
				break;
			default:
				return;
		}
	}

	// This function follows the "Activation order for Board Elements and Robot
	// Lasers" from the rulebook page 8
	public void registerActions(Player[] sortedPlayers, int register, int priority, Map<Vector2D, Space> spaceMap) {
		Space currSpace = spaceMap.get(this.currentSpace);
		List<BoardElement> attachments = currSpace.getBoardElements();
		for (Player player : sortedPlayers) {
			// Follows the activation order
			for (int i = 0; i < attachments.size(); i++) {
				BoardElement element = attachments.get(i);

				switch (priority) {
					case 1:
						// Conveyor
						if (element instanceof ConveyorBelt) {
							ConveyorBelt conveyorBelt = (ConveyorBelt) element;
							// Second: Green conveyor belt
							if ((conveyorBelt.getType() == ConveyorBeltType.GREEN_STRAIGHT || conveyorBelt
									.getType() == ConveyorBeltType.GREEN_RIGHT_CURVE || conveyorBelt
											.getType() == ConveyorBeltType.GREEN_LEFT_CURVE)) {
								currentSpace.add(conveyorBelt.getDirection().step());
								conveyorRotate(conveyorBelt);
							}

						}
						break;

					case 2:
						// Conveyor
						if (element instanceof ConveyorBelt) {
							ConveyorBelt conveyorBelt = (ConveyorBelt) element;
							// Second: Green conveyor belt
							if ((conveyorBelt.getType() == ConveyorBeltType.GREEN_STRAIGHT || conveyorBelt
									.getType() == ConveyorBeltType.GREEN_RIGHT_CURVE || conveyorBelt
											.getType() == ConveyorBeltType.GREEN_LEFT_CURVE)) {
								currentSpace.add(conveyorBelt.getDirection().step());
								conveyorRotate(conveyorBelt);
							}

						}
						break;

						case 3:
							// Third: Push Panel
							if (element instanceof PushPanel) {
								PushPanel pp = (PushPanel) element;
								currentSpace.add(pp.getPushDirection().step());
							}
							break;

						case 4:
							// Fourth: Gears
							if (element instanceof Gear) {
								Gear gear = (Gear) element;
								switch (gear.getRotationDirection()) {
									case CLOCKWISE:
										turnRight();
										break;

									case COUNTERCLOCKWISE:
										turnLeft();
										break;
								}
							}
							break;

						case 5: case 6:
							// Fifth: Board Lasers
							// TODO

							// Sixth: Robot Lasers
							// TODO
							break;

						case 7:
							// Seventh: Energy Space
							if (element instanceof EnergySpace) {
								EnergySpace es = (EnergySpace) element;
								if (register == 5) {
									player.addEnergy(1);
								}
								if (es.getCubeAmount() >= 1) {
									player.addEnergy(1);
									es.addCubeAmount(-1);
								}
							}
							break;

						case 8:
							// Eighth: Checkpoint
							if (element instanceof Checkpoint) {
								Checkpoint cp = (Checkpoint) element;
								if (player.getCheckpointsReached() == cp.getNumber() - 1) {
									player.reachNextCheckpoint();
								}
							}
							break;
				}
			}
		}
	}
}
