package util;

import java.util.Locale;

public class Point2D {
    private float x;
    private float y;

    public Point2D() {
        this(0, 0);
    }

    public Point2D(Point2D other) {
        this(other.x, other.y);
    }

    public Point2D(float x, float y) {
        set(x, y);
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(Point2D other) {
        set(other.x, other.y);
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

    public Point2D subtract(Point2D other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public static float dist(Point2D A, Point2D B) {
        return (float)Math.sqrt((A.x() - B.x()) * (A.x() - B.x()) +
                (A.y() - B.y()) * (A.y() - B.y()));
    }

    public float mag() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public Point2D normalize() {
        float mag = mag();
        return divide(mag);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "Point2D(%f,%f)", x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Point2D po = (Point2D) o;
        return x == po.x && y == po.y;
    }
}