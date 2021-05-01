package util;

public class Interpolation {

    public static Point2D interpolate(Point2D positionA, Point2D positionB, float factor) {
        return new Point2D(
                interpolate(positionA.x(), positionB.x(), factor),
                interpolate(positionA.y(), positionB.y(), factor)
        );
    }

    // TODO: fix rotation interpolation (-180 == 180)
    public static float interpolate(float rotationA, float rotationB, float factor) {
        return rotationA + (rotationB - rotationA) * factor;
    }

    private Interpolation() {}
}
