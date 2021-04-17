package physics;

import util.Point2D;

public interface Hitbox {

    float getRadius();
    Point2D getPosition();

    void setPosition(Point2D resolvedPosition);
}
