package dk.dtu.roborally.handlers;

import java.util.List;

import dk.dtu.roborally.handlers.Level.Square;
import dk.dtu.roborally.objects.Wall;

/**
 * This object is used to store the loaded info from the levels via the Config
 *
 * A little upgrade we can do at some point is to actually load it into the
 * correct classes instead of classes under Level
 * 
 * @author Victor
 */

public class Level {
    public List<Square> squares;
    public List<Belt> belts;
    public List<StartingPoint> start_points;
    public List<Wall> walls;

    public static class Square {
        public int x1;
        public int y1;
        public int x2;
        public int y2;
    }

    public static class Belt {
        public int x1;
        public int y1;
        public int x2;
        public int y2;
    }

    public static class StartingPoint {
        public int x;
        public int y;
    }

    public static class Wall {
        public int x1;
        public int y1;
        public int x2;
        public int y2;
    }

}
