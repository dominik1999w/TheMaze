package entity;

import java.util.UUID;

import util.Point2D;

public interface WorldEntity {
    UUID getId();
    Point2D getPosition();
    float getRotation();
}
