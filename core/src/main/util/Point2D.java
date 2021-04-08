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
}
