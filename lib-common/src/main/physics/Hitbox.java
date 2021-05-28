package physics;

import java.util.UUID;

import util.Point2D;

public interface Hitbox {

    UUID getId();
    HitboxType getType();

    float getRadius();
    Point2D getPosition();

    void notifyMapCollision(Point2D resolvedPosition);
    void notifyEntityCollision(Hitbox hitbox);
}
