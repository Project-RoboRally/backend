package dk.dtu.roborally.helpers;

public class MathHelper {
    public static int sign(double value) {
        return value > 0 ? 1 : (value < 0 ? -1 : 0);
    }
}
