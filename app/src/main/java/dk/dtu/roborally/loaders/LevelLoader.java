package dk.dtu.roborally.loaders;

import java.util.ArrayList;

import dk.dtu.roborally.enums.ConveyorBeltType;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.helpers.MathHelper;
import dk.dtu.roborally.objects.Board;
import dk.dtu.roborally.objects.ConveyorBelt;
import dk.dtu.roborally.objects.LaserEmitter;
import dk.dtu.roborally.objects.StartingPoint;
import dk.dtu.roborally.objects.Vector2D;
import dk.dtu.roborally.objects.Wall;

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
        ArrayList<ConveyorBelt> cBelts = new ArrayList<>();
        ArrayList<Wall> walls = new ArrayList<>();
        ArrayList<LaserEmitter> laserEmitters = new ArrayList<>();
        ArrayList<StartingPoint> startingPoints = new ArrayList<>();
        // Load belts
        for (int i = 0; i < level.belts.length; i++) {
            cBelts.add(createConveyorBelt(level.belts[i]));
        }
        // Load Walls
        for (int i = 0; i < level.walls.length; i++) {
            createWalls(level.walls[i], walls);
        }
        // Load LaserEmitters
        for (int i = 0; i < level.laser_emitters.length; i++) {
            laserEmitters.add(createLaserEmitter(level.laser_emitters[i]));
        }
        // Load StartingPoints
        for (int i = 0; i < level.start_points.length; i++) {
            startingPoints.add(createStartingPoint(level.start_points[i]));
        }
        return null;
    }

    private static ConveyorBelt createConveyorBelt(LevelData.BeltData belt) {
        ConveyorBeltType cbt;

        switch (belt.type) {
            case ("blue"):
                cbt = ConveyorBeltType.BLUE;
                break;
            case ("green"):
                cbt = ConveyorBeltType.GREEN;
                break;
            default:
                cbt = null;
                System.out.println("Loaded incorrect Conveyorbelt Type in LevelLoader.java");
                break;
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
                dir = null;
                System.out.println("Loaded incorrect Direction for ConveyorBelt in LevelLoader.java");
                break;
        }

        return new ConveyorBelt(new Vector2D(belt.x, belt.y), belt.length, dir, cbt);
    }

    private static void createWalls(LevelData.WallData wall, ArrayList<Wall> wallList) {
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
                dir = null;
                System.out.println("Loaded incorrect Direction for Walls in LevelLoader.java");
                break;
        }
        // Make connected walls
        int horizontalLength = Math.abs(wall.x1 - wall.x2) + 1;
        int verticalLength = Math.abs(wall.y1 - wall.y2) + 1;
        for (int _x = wall.x1; _x < wall.x1 + horizontalLength * MathHelper.sign(_x); _x += MathHelper.sign(_x)) {
            for (int _y = wall.y1; _y < wall.y1 + verticalLength * MathHelper.sign(_y); _y += MathHelper.sign(_y)) {
                wallList.add(new Wall(new Vector2D(_x, _y), dir));
            }
        }
    }

    private static LaserEmitter createLaserEmitter(LevelData.LaserData laser) {
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
                dir = null;
                System.out.println("Loaded incorrect Direction for ConveyorBelt in LevelLoader.java");
                break;
        }

        return new LaserEmitter(new Vector2D(laser.x, laser.y), dir);
    }

    private static StartingPoint createStartingPoint(LevelData.StartingPointData stp) {
        return new StartingPoint(new Vector2D(stp.x, stp.y));
    }

}
