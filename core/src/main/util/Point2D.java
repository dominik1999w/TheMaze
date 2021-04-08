package util;

public class Point2D {
    public float x;
    public float y;

    public Point2D() {

    }

    public Point2D(float x, float y) {
        set(x,y);
    }

    public Point2D(Point2D other) {
        set(other.x, other.y);
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point2D add(Point2D other) {
        return new Point2D(x+other.x, y+other.y);
    }

    public Point2D multiply(float scalar) {
        return new Point2D(x*scalar, y*scalar);
    }

    public Point2D divide(float scalar) {
        return new Point2D(x/scalar, y/scalar);
    }
}
