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
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Point2D multiply(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public Point2D divide(float scalar) {
        this.x /= scalar;
        this.y /= scalar;
        return this;
    }
}