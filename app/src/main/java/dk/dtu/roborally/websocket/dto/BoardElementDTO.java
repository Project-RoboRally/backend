package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.enums.ConveyorBeltType;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.RotationDirection;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.Checkpoint;
import dk.dtu.roborally.models.board.elements.ConveyorBelt;
import dk.dtu.roborally.models.board.elements.EnergySpace;
import dk.dtu.roborally.models.board.elements.Gear;
import dk.dtu.roborally.models.board.elements.LaserEmitter;
import dk.dtu.roborally.models.board.elements.Pit;
import dk.dtu.roborally.models.board.elements.PriorityAntenna;
import dk.dtu.roborally.models.board.elements.PushPanel;
import dk.dtu.roborally.models.board.elements.RebootPoint;
import dk.dtu.roborally.models.board.elements.StartingPoint;
import dk.dtu.roborally.models.board.elements.Wall;

/**
 * A board element placed on a space. Every variant carries a {@code type} field
 * so clients can tell them apart.
 *
 * @author Elias
 */
public sealed interface BoardElementDTO {

	record WallDTO(String type, Direction side) implements BoardElementDTO {
	}

	record ConveyorBeltDTO(String type, Direction direction,
			ConveyorBeltType beltType, int length) implements BoardElementDTO {
	}

	record GearDTO(String type,
			RotationDirection rotationDirection) implements BoardElementDTO {
	}

	record LaserEmitterDTO(String type, Direction direction,
			int damage) implements BoardElementDTO {
	}

	record PushPanelDTO(String type,
			Direction pushDirection) implements BoardElementDTO {
	}

	record CheckpointDTO(String type, int number) implements BoardElementDTO {
	}

	record EnergySpaceDTO(String type,
			int cubeAmount) implements BoardElementDTO {
	}

	record SimpleElementDTO(String type) implements BoardElementDTO {
	}

	static BoardElementDTO from(BoardElement element) {
		return switch (element) {
			case Wall wall -> new WallDTO("WALL", wall.getSide());
			case ConveyorBelt belt -> new ConveyorBeltDTO("CONVEYOR_BELT",
					belt.getDirection(), belt.getType(), belt.getLength());
			case Gear gear -> new GearDTO("GEAR", gear.getRotationDirection());
			case LaserEmitter laser -> new LaserEmitterDTO("LASER_EMITTER",
					laser.getDirection(), laser.getDamage());
			case PushPanel panel ->
				new PushPanelDTO("PUSH_PANEL", panel.getPushDirection());
			case Checkpoint checkpoint ->
				new CheckpointDTO("CHECKPOINT", checkpoint.getNumber());
			case EnergySpace energy ->
				new EnergySpaceDTO("ENERGY_SPACE", energy.getCubeAmount());
			case Pit ignored -> new SimpleElementDTO("PIT");
			case StartingPoint ignored ->
				new SimpleElementDTO("STARTING_POINT");
			case RebootPoint ignored -> new SimpleElementDTO("REBOOT_POINT");
			case PriorityAntenna ignored ->
				new SimpleElementDTO("PRIORITY_ANTENNA");
			default -> throw new IllegalArgumentException(
					"Unknown board element: " + element.getClass().getName());
		};
	}
}
