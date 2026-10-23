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
	public void restActions(Player player, Map<Vector2D, Space> spaceMap) {
		Space currSpace = spaceMap.get(this.currentSpace);
		List<BoardElement> attachments = currSpace.getBoardElements();
		// Follows the activation order
		for (int priority = 1; priority <= 8; priority++) { // used for the ordering of priorities
			for (int i = 0; i < attachments.size(); i++) {
				BoardElement element = attachments.get(i);

				// Conveyor
				if (element instanceof ConveyorBelt) {
					ConveyorBelt conveyorBelt = (ConveyorBelt) element;
					// First: Blue conveyor belt
					if (priority == 1 && (conveyorBelt.getType() == ConveyorBeltType.BLUE_STRAIGHT || conveyorBelt
							.getType() == ConveyorBeltType.BLUE_RIGHT_CURVE || conveyorBelt
									.getType() == ConveyorBeltType.BLUE_LEFT_CURVE)) {
						// Move 1
						currentSpace.add(conveyorBelt.getDirection().step());
						conveyorRotate(conveyorBelt);
						// Move 2
						Space newSpace = spaceMap.get(this.currentSpace);
						List<BoardElement> a = newSpace.getBoardElements();
						for (int j = 0; j < a.size(); j++) {
							BoardElement e = a.get(i);
							if (e instanceof ConveyorBelt) {
								ConveyorBelt c = (ConveyorBelt) e;
								currentSpace.add(c.getDirection().step());
								conveyorRotate(c);
							}
						}
					}
					// Second: Green conveyor belt
					if (priority == 2 && (conveyorBelt.getType() == ConveyorBeltType.GREEN_STRAIGHT || conveyorBelt
							.getType() == ConveyorBeltType.GREEN_RIGHT_CURVE || conveyorBelt
									.getType() == ConveyorBeltType.GREEN_LEFT_CURVE)) {
						currentSpace.add(conveyorBelt.getDirection().step());
						conveyorRotate(conveyorBelt);
					}

				}

				// Third: Push Panel
				if (element instanceof PushPanel && priority == 3) {
					PushPanel pp = (PushPanel) element;
					currentSpace.add(pp.getPushDirection().step());
				}

				// Fourth: Gears
				if (element instanceof Gear && priority == 4) {
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

				// Fifth: Board Lasers
				// TODO

				// Sixth: Robot Lasers
				// TODO

				// Seventh: Energy Space
				// TODO, cause we do not have energy yet

				// Eighth: Checkpoint
				if (element instanceof Checkpoint && priority == 8) {
					Checkpoint cp = (Checkpoint) element;
					if (player.getCheckpointsReached() == cp.getNumber() - 1) {
						player.setCheckpointsReached(cp.getNumber());
					}
				}
			}
		}
	}
}
