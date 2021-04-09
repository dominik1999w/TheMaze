package util;

public class Shape2D {
    private final Point2D position;
    private final Point2D size;

    public Shape2D(Point2D position, Point2D size) {
        this.position = position;
        this.size = size;
    }

    public Point2D getPosition() {
        return position;
    }

    public Point2D getSize() {
        return size;
    }
}
