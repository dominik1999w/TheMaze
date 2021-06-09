package util;

public class Interpolation {

    public static Point2D interpolate(Point2D positionA, Point2D positionB, float factor) {
        return new Point2D(
                interpolate(positionA.x(), positionB.x(), factor),
                interpolate(positionA.y(), positionB.y(), factor)
        );
    }

    public static float interpolate(float valueA, float valueB, float factor) {
        return valueA + (valueB - valueA) * factor;
    }

    /*
     * -180 < rotation < 180
     */
    public static float interpolateRotation(float rotationA, float rotationB, float factor) {
        if (rotationB - rotationA > 180) {
            rotationA += 360;
        } else if (rotationA - rotationB > 180) {
            rotationB += 360;
        }
        return interpolate(rotationA, rotationB, factor);
    }

    private Interpolation() {}
}
