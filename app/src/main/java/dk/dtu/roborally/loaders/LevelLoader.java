package dk.dtu.roborally.loaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dk.dtu.roborally.enums.ConveyorBeltType;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.RotationDirection;
import dk.dtu.roborally.models.Board;
import dk.dtu.roborally.models.Checkpoint;
import dk.dtu.roborally.models.ConveyorBelt;
import dk.dtu.roborally.models.EnergySpace;
import dk.dtu.roborally.models.Gear;
import dk.dtu.roborally.models.LaserEmitter;
import dk.dtu.roborally.models.Pit;
import dk.dtu.roborally.models.PushPanel;
import dk.dtu.roborally.models.StartingPoint;
import dk.dtu.roborally.models.Tile;
import dk.dtu.roborally.models.Vector2D;
import dk.dtu.roborally.models.Wall;

/**
 * This class loads the data from Level.java into the correct classes from the
 * objects folder
 * 
 * @author Victor
 */

public class LevelLoader {
    public static Board load(String levelPath) {
        // We need to load all the data into a board here.
        // We just need to use the methods below to load everything correctly.
        LevelData level = JsonLoader.loadLevel(levelPath);
        Map<Vector2D, Tile> tileMap = new HashMap<>();
        ArrayList<ConveyorBelt> cBelts = new ArrayList<>();
        ArrayList<Wall> walls = new ArrayList<>();
        ArrayList<LaserEmitter> laserEmitters = new ArrayList<>();
        ArrayList<StartingPoint> startingPoints = new ArrayList<>();
        ArrayList<Checkpoint> checkpoints = new ArrayList<>();
        ArrayList<Gear> gears = new ArrayList<>();
        ArrayList<PushPanel> pushPanels = new ArrayList<>();
        ArrayList<Pit> pits = new ArrayList<>();
        ArrayList<EnergySpace> energySpaces = new ArrayList<>();
        // Load Tiles
        for (int i = 0; i < level.squares.length; i++) {
            createTiles(level.squares[i], tileMap);
        }
        // Make board from the tiles
        Board board = new Board(tileMap);
        // Load belts
        for (int i = 0; i < level.belts.length; i++) {
            cBelts.add(createConveyorBelt(level.belts[i], tileMap));
        }
        // Load Walls
        for (int i = 0; i < level.walls.length; i++) {
            createWalls(level.walls[i], walls, tileMap);
        }
        // Load LaserEmitters
        for (int i = 0; i < level.laser_emitters.length; i++) {
            laserEmitters.add(createLaserEmitter(level.laser_emitters[i], tileMap));
        }
        // Load StartingPoints
        for (int i = 0; i < level.start_points.length; i++) {
            startingPoints.add(createStartingPoint(level.start_points[i], tileMap));
        }
        // Load Checkpoints
        for (int i = 0; i < level.checkpoints.length; i++) {
            checkpoints.add(createCheckpoint(level.checkpoints[i], tileMap));
        }
        // Load Gears
        for (int i = 0; i < level.gears.length; i++) {
            gears.add(createGear(level.gears[i], tileMap));
        }
        // Load Push Panels
        for (int i = 0; i < level.push_panels.length; i++) {
            pushPanels.add(createPushPanel(level.push_panels[i], tileMap));
        }
        // Load Pits
        for (int i = 0; i < level.pits.length; i++) {
            createPits(level.pits[i], pits, tileMap);
        }
        // Load EnergySpaces
        for (int i = 0; i < level.energy_spaces.length; i++) {
            energySpaces.add(createEnergySpace(level.energy_spaces[i], tileMap));
        }
        return board;
    }

    public static Tile getTileAt(Map<Vector2D, Tile> tileMap, Vector2D position) {
        if (tileMap.containsKey(position)) {
            return tileMap.get(position);
        }

        throw new IllegalArgumentException(
                "Invalid position. Can't access tile at: " + position.toString());
    }

    private static ConveyorBelt createConveyorBelt(LevelData.BeltData belt, Map<Vector2D, Tile> tileMap) {
        ConveyorBeltType cbt;

        switch (belt.type) {
            case ("blue"):
                cbt = ConveyorBeltType.BLUE;
                break;
            case ("green"):
                cbt = ConveyorBeltType.GREEN;
                break;
            default:
                throw new IllegalArgumentException(
                        "Invalid conveyor belt type: " + belt.type);
        }

        Direction dir;

        switch (belt.direction) {
            case "east":
                dir = Direction.EAST;
                break;

            case "north":
                dir = Direction.NORTH;
                break;

            case "west":
                dir = Direction.WEST;
                break;

            case "south":
                dir = Direction.SOUTH;
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid conveyor belt direction: " + belt.direction);
        }
        ConveyorBelt cb = new ConveyorBelt(new Vector2D(belt.x, belt.y), belt.length, dir, cbt);
        switch (dir) {
            case Direction.NORTH: // we need to change this if the frontenders are using negative Y as down
                for (int i = belt.y; i > belt.y - belt.length; i--) {
                    getTileAt(tileMap, new Vector2D(belt.x, i)).addBoardElement(cb);
                }
                break;

            case Direction.SOUTH: // we need to change this if the frontenders are using negative Y as down
                for (int i = belt.y; i < belt.y + belt.length; i++) {
                    getTileAt(tileMap, new Vector2D(belt.x, i)).addBoardElement(cb);
                }
                break;

            case Direction.WEST:
                for (int i = belt.x; i > belt.x - belt.length; i--) {
                    getTileAt(tileMap, new Vector2D(i, belt.y)).addBoardElement(cb);
                }
                break;

            case Direction.EAST:
                for (int i = belt.x; i < belt.x + belt.length; i++) {
                    getTileAt(tileMap, new Vector2D(i, belt.y)).addBoardElement(cb);
                }
                break;
        }
        return cb;
    }

    private static void createTiles(LevelData.SquareData square, Map<Vector2D, Tile> tileMap) {
        int minX = Math.min(square.x1, square.x2);
        int maxX = Math.max(square.x1, square.x2);

        int minY = Math.min(square.y1, square.y2);
        int maxY = Math.max(square.y1, square.y2);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                Vector2D position = new Vector2D(x, y);
                tileMap.put(position, new Tile(position));
            }
        }
    }

    private static void createWalls(LevelData.WallData wall, ArrayList<Wall> wallList, Map<Vector2D, Tile> tileMap) {
        Direction dir;
        // Get the direction for wall
        switch (wall.direction) {
            case "east":
                dir = Direction.EAST;
                break;

            case "north":
                dir = Direction.NORTH;
                break;

            case "south":
                dir = Direction.SOUTH;
                break;

            case "west":
                dir = Direction.WEST;
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid wall direction: " + wall.direction);
        }
        // Make connected walls
        int minX = Math.min(wall.x1, wall.x2);
        int maxX = Math.max(wall.x1, wall.x2);

        int minY = Math.min(wall.y1, wall.y2);
        int maxY = Math.max(wall.y1, wall.y2);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                Wall w = new Wall(new Vector2D(x, y), dir);
                getTileAt(tileMap, w.getPosition()).addBoardElement(w);
                wallList.add(w);
            }
        }
    }

    private static LaserEmitter createLaserEmitter(LevelData.LaserData laser, Map<Vector2D, Tile> tileMap) {
        Direction dir;
        switch (laser.direction) {
            case "east":
                dir = Direction.EAST;
                break;

            case "north":
                dir = Direction.NORTH;
                break;

            case "west":
                dir = Direction.WEST;
                break;

            case "south":
                dir = Direction.SOUTH;
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid laser direction: " + laser.direction);
        }
        LaserEmitter le = new LaserEmitter(new Vector2D(laser.x, laser.y), dir, laser.amount);
        getTileAt(tileMap, le.getPosition()).addBoardElement(le);
        return le;
    }

    private static StartingPoint createStartingPoint(LevelData.StartingPointData stp, Map<Vector2D, Tile> tileMap) {
        StartingPoint startPoint = new StartingPoint(new Vector2D(stp.x, stp.y));
        getTileAt(tileMap, startPoint.getPosition()).addBoardElement(startPoint);
        return startPoint;
    }

    private static Checkpoint createCheckpoint(LevelData.CheckpointData cp, Map<Vector2D, Tile> tileMap) {
        Checkpoint checkpoint = new Checkpoint(new Vector2D(cp.x, cp.y), cp.number);
        getTileAt(tileMap, checkpoint.getPosition()).addBoardElement(checkpoint);
        return checkpoint;
    }

    private static Gear createGear(LevelData.GearData gear, Map<Vector2D, Tile> tileMap) {
        RotationDirection rd;
        switch (gear.rotation_direction) {
            case "clockwise":
                rd = RotationDirection.CLOCKWISE;
                break;

            case "counterclockwise":
                rd = RotationDirection.COUNTERCLOCKWISE;
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid gear rotation direction: " + gear.rotation_direction);
        }
        Gear g = new Gear(new Vector2D(gear.x, gear.y), rd);
        getTileAt(tileMap, g.getPosition()).addBoardElement(g);
        return g;
    }

    private static PushPanel createPushPanel(LevelData.PushPanelData pPanel, Map<Vector2D, Tile> tileMap) {
        Direction dir;
        switch (pPanel.push_direction) {
            case "east":
                dir = Direction.EAST;
                break;

            case "north":
                dir = Direction.NORTH;
                break;

            case "west":
                dir = Direction.WEST;
                break;

            case "south":
                dir = Direction.SOUTH;
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid push panel push direction: " + pPanel.push_direction);
        }
        PushPanel pp = new PushPanel(new Vector2D(pPanel.x, pPanel.y), dir);
        getTileAt(tileMap, pp.getPosition()).addBoardElement(pp);
        return pp;
    }

    private static void createPits(LevelData.PitData pit, ArrayList<Pit> pitList, Map<Vector2D, Tile> tileMap) {
        // Make connected walls
        int minX = Math.min(pit.x1, pit.x2);
        int maxX = Math.max(pit.x1, pit.x2);

        int minY = Math.min(pit.y1, pit.y2);
        int maxY = Math.max(pit.y1, pit.y2);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                Pit p = new Pit(new Vector2D(x, y));
                getTileAt(tileMap, p.getPosition()).addBoardElement(p);
                pitList.add(p);
            }
        }
    }

    private static EnergySpace createEnergySpace(LevelData.EnergySpaceData esp, Map<Vector2D, Tile> tileMap) {
        EnergySpace energySpace = new EnergySpace(new Vector2D(esp.x, esp.y), esp.energy_cubes);
        getTileAt(tileMap, energySpace.getPosition()).addBoardElement(energySpace);
        return energySpace;
    }

}
