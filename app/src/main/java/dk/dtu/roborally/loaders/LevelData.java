package dk.dtu.roborally.loaders;

/**
 * This object is used to store and extract the loaded info from the levels via
 * the Config and other loaders
 * 
 * @author Victor
 */

public class LevelData {
    public SquareData[] squares;
    public BeltData[] belts;
    public StartingPointData[] start_points;
    public WallData[] walls;
    public LaserData[] laser_emitters;
    public PitData[] pits;
    public EnergySpaceData[] energy_spaces;
    public PushPanelData[] push_panels;
    public GearData[] gears;
    public CheckpointData[] checkpoints;

    public static class SquareData {
        public int x1;
        public int y1;
        public int x2;
        public int y2;
    }

    public static class BeltData {
        public int x;
        public int y;
        public int length;
        public String direction;
        public String type;
    }

    public static class CheckpointData {
        public int x;
        public int y;
        public int number;
    }

    public static class StartingPointData {
        public int x;
        public int y;
    }

    public static class WallData {
        public int x1;
        public int y1;
        public int x2;
        public int y2;
        public String direction;
    }

    public static class LaserData {
        public int x;
        public int y;
        public int amount;
        public String direction;
    }

    public static class PitData {
        public int x1;
        public int y1;
        public int x2;
        public int y2;
    }

    public static class EnergySpaceData {
        public int x;
        public int y;
        public int energy_cubes;
    }

    public static class PushPanelData {
        public int x;
        public int y;
        public String push_direction;
    }

    public static class GearData {
        public int x;
        public int y;
        public String rotation_direction;
    }

}
