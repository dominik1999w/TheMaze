package util;

import java.util.Locale;

public class Point2Di {

    private int x;
    private int y;

    public Point2Di() {
        this(0, 0);
    }

    public Point2Di(Point2Di other) {
        this(other.x, other.y);
    }

    public Point2Di(int x, int y) {
        set(x, y);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point2Di add(Point2Di other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Point2Di subtract(Point2Di other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public Point2Di min(Point2Di other) {
        if (other.x < this.x)
            this.x = other.x;
        if (other.y < this.y)
            this.y = other.y;
        return this;
    }

    public Point2Di max(Point2Di other) {
        if (other.x > this.x)
            this.x = other.x;
        if (other.y > this.y)
            this.y = other.y;
        return this;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "Point2Di(%d,%d)", x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Point2Di po = (Point2Di) o;
        return x == po.x && y == po.y;
    }
}
