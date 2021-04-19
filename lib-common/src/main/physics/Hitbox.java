package physics;

import java.util.UUID;

import util.Point2D;

public interface Hitbox {

    UUID getId();
    HitboxType getType();

    float getRadius();
    Point2D getPosition();

    void setPosition(Point2D resolvedPosition);

    void notifyMapCollision();
    //void notifyCollision(Hitbox hitbox);
}
