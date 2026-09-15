package dk.dtu.roborally.loaders;

import java.util.List;

/**
 * This object is used to store the loaded info from the levels via the Config
 * 
 * @author Victor
 */

public class LevelData {
    public SquareData[] squares;
    public BeltData[] belts;
    public StartingPointData[] start_points;
    public WallData[] walls;
    public LaserData[] laser_emitters;

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
        public String direction;
    }

}
