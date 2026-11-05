package dk.dtu.roborally.loaders;

import dk.dtu.roborally.enums.ConveyorBeltType;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.RotationDirection;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Builds a board from a level file, placing every board element on the spaces
 * it covers.
 *
 * @author Victor, Sebastian
 */

public class LevelLoader {
	public static Board load(String levelPath) {
		LevelData level = JsonLoader.loadLevel(levelPath);
		if (level == null) {
			throw new IllegalArgumentException(
					"Level data is null for: " + levelPath);
		}
		if (level.squares == null || level.squares.length == 0) {
			throw new IllegalArgumentException(
					"Level must contain at least one square: " + levelPath);
		}

		Map<Vector2D, Space> spaceMap = new HashMap<>();

		for (LevelData.SquareData square : level.squares) {
			createSpaces(square, spaceMap);
		}
		// Everything but the squares is optional in a level file
		each(level.belts, belt -> createConveyorBelt(belt, spaceMap));
		each(level.walls, wall -> createWalls(wall, spaceMap));
		each(level.laser_emitters,
				laser -> createLaserEmitter(laser, spaceMap));
		each(level.start_points, point -> createStartingPoint(point, spaceMap));
		each(level.checkpoints,
				checkpoint -> createCheckpoint(checkpoint, spaceMap));
		each(level.gears, gear -> createGear(gear, spaceMap));
		each(level.push_panels, panel -> createPushPanel(panel, spaceMap));
		each(level.pits, pit -> createPits(pit, spaceMap));
		each(level.energy_spaces,
				energySpace -> createEnergySpace(energySpace, spaceMap));
		each(level.reboot_points, point -> createRebootPoint(point, spaceMap));
		if (level.antenna != null) {
			createPriorityAntenna(level.antenna, spaceMap);
		}
		return new Board(spaceMap);
	}

	private static <T> void each(T[] items, Consumer<T> create) {
		if (items == null) {
			return;
		}
		for (T item : items) {
			create.accept(item);
		}
	}

	public static Space getSpaceAt(Map<Vector2D, Space> spaceMap,
			Vector2D position) {
		if (spaceMap.containsKey(position)) {
			return spaceMap.get(position);
		}

		throw new IllegalArgumentException(
				"Invalid position. Can't access space at: "
						+ position.toString());
	}

	/**
	 * Reads an enum value written in lowercase in the level file, like "north"
	 * or "clockwise".
	 */
	private static <E extends Enum<E>> E parse(Class<E> type, String value,
			String field) {
		if (value != null) {
			for (E constant : type.getEnumConstants()) {
				if (constant.name().equalsIgnoreCase(value)) {
					return constant;
				}
			}
		}
		throw new IllegalArgumentException("Invalid " + field + ": " + value);
	}

	private static void createConveyorBelt(LevelData.BeltData belt,
			Map<Vector2D, Space> spaceMap) {
		ConveyorBeltType cbt = parse(ConveyorBeltType.class, belt.type,
				"conveyor belt type");

		Direction dir = parse(Direction.class, belt.direction,
				"conveyor belt direction");
		Vector2D position = new Vector2D(belt.x, belt.y);
		ConveyorBelt cb = new ConveyorBelt(position, belt.length, dir, cbt);
		// One belt object covers every space it runs over
		for (int i = 0; i < belt.length; i++) {
			getSpaceAt(spaceMap, position).addBoardElement(cb);
			position = position.add(dir.step());
		}
	}

	private static void createSpaces(LevelData.SquareData square,
			Map<Vector2D, Space> spaceMap) {
		int minX = Math.min(square.x1, square.x2);
		int maxX = Math.max(square.x1, square.x2);

		int minY = Math.min(square.y1, square.y2);
		int maxY = Math.max(square.y1, square.y2);

		for (int x = minX; x < maxX; x++) {
			for (int y = minY; y < maxY; y++) {
				Vector2D position = new Vector2D(x, y);
				spaceMap.put(position, new Space(position));
			}
		}
	}

	private static void createWalls(LevelData.WallData wall,
			Map<Vector2D, Space> spaceMap) {
		Direction dir = parse(Direction.class, wall.direction,
				"wall direction");
		for (Vector2D position : area(wall.x1, wall.y1, wall.x2, wall.y2)) {
			getSpaceAt(spaceMap, position)
					.addBoardElement(new Wall(position, dir));
		}
	}

	private static void createLaserEmitter(LevelData.LaserData laser,
			Map<Vector2D, Space> spaceMap) {
		Direction dir = parse(Direction.class, laser.direction,
				"laser direction");
		LaserEmitter le = new LaserEmitter(new Vector2D(laser.x, laser.y), dir,
				laser.amount);
		getSpaceAt(spaceMap, le.getPosition()).addBoardElement(le);
	}

	private static void createStartingPoint(LevelData.StartingPointData stp,
			Map<Vector2D, Space> spaceMap) {

		Direction orientation = parse(Direction.class, stp.orientation,
				"starting orientation");

		StartingPoint startPoint = new StartingPoint(new Vector2D(stp.x, stp.y),
				orientation);

		getSpaceAt(spaceMap, startPoint.getPosition())
				.addBoardElement(startPoint);
	}

	private static void createCheckpoint(LevelData.CheckpointData cp,
			Map<Vector2D, Space> spaceMap) {
		Checkpoint checkpoint = new Checkpoint(new Vector2D(cp.x, cp.y),
				cp.number);
		getSpaceAt(spaceMap, checkpoint.getPosition())
				.addBoardElement(checkpoint);
	}

	private static void createPriorityAntenna(LevelData.AntennaData antenna,
			Map<Vector2D, Space> spaceMap) {
		Direction facing = antenna.direction == null
				? Direction.NORTH
				: parse(Direction.class, antenna.direction,
						"antenna direction");
		getSpaceAt(spaceMap, new Vector2D(antenna.x, antenna.y))
				.addBoardElement(new PriorityAntenna(facing));
	}

	private static void createRebootPoint(LevelData.RebootPointData point,
			Map<Vector2D, Space> spaceMap) {
		Vector2D position = new Vector2D(point.x, point.y);
		getSpaceAt(spaceMap, position).addBoardElement(new RebootPoint(position,
				parse(Direction.class, point.direction, "reboot direction"),
				new Vector2D(point.x1, point.y1),
				new Vector2D(point.x2, point.y2)));
	}

	private static void createGear(LevelData.GearData gear,
			Map<Vector2D, Space> spaceMap) {
		RotationDirection rd = parse(RotationDirection.class,
				gear.rotation_direction, "gear rotation direction");
		Gear g = new Gear(new Vector2D(gear.x, gear.y), rd);
		getSpaceAt(spaceMap, g.getPosition()).addBoardElement(g);
	}

	private static void createPushPanel(LevelData.PushPanelData pPanel,
			Map<Vector2D, Space> spaceMap) {
		Direction dir = parse(Direction.class, pPanel.push_direction,
				"push panel push direction");
		PushPanel pp = new PushPanel(new Vector2D(pPanel.x, pPanel.y), dir);
		getSpaceAt(spaceMap, pp.getPosition()).addBoardElement(pp);
	}

	private static void createPits(LevelData.PitData pit,
			Map<Vector2D, Space> spaceMap) {
		for (Vector2D position : area(pit.x1, pit.y1, pit.x2, pit.y2)) {
			getSpaceAt(spaceMap, position).addBoardElement(new Pit(position));
		}
	}

	/** Every position in the rectangle between two corners, both included. */
	private static List<Vector2D> area(int x1, int y1, int x2, int y2) {
		List<Vector2D> positions = new ArrayList<>();
		for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
			for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
				positions.add(new Vector2D(x, y));
			}
		}
		return positions;
	}

	private static void createEnergySpace(LevelData.EnergySpaceData esp,
			Map<Vector2D, Space> spaceMap) {
		EnergySpace energySpace = new EnergySpace(new Vector2D(esp.x, esp.y),
				esp.energy_cubes);
		getSpaceAt(spaceMap, energySpace.getPosition())
				.addBoardElement(energySpace);
	}

}
