package util;

/*
 * From gdx.MathUtils
 */
public class MathUtils {

    static private final int BIG_ENOUGH_INT = 16 * 1024;
    static private final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;

    /** Returns the largest integer less than or equal to the specified float. This method will only properly floor floats from
     * -(2^14) to (Float.MAX_VALUE - 2^14). */
    public static int floor (float value) {
        return (int)(value + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
    }

    public static float clamp (float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
