package util;

public class Interpolation {

    public static Point2D interpolate(Point2D positionA, Point2D positionB, float factor) {
        return new Point2D(positionA).add(new Point2D(positionB).subtract(positionA).multiply(factor));
    }

    public static float interpolate(float rotationA, float rotationB, float factor) {
        return rotationA + (rotationB - rotationA) * factor;
    }

    private Interpolation() {}
}
