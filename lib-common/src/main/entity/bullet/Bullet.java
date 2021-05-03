package entity.bullet;

import java.util.UUID;

import entity.WorldEntity;
import util.Point2D;

public class Bullet implements WorldEntity {
    private final UUID id;

    private final Point2D position;
    private final float rotation;
    private final float speed;

    public Bullet(UUID id, Point2D position, float rotation) {
        this.id = id;
        this.position = position;
        this.rotation = rotation;
        this.speed = BulletConfig.INITIAL_SPEED;
    }

    public Bullet(Point2D position, float angle) {
        this(UUID.randomUUID(), position, angle);
    }

    public void setPosition(Point2D position) {
        this.position.set(position);
    }

    public Point2D getPosition() {
        return position;
    }

    public float getRotation() {
        return rotation;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public UUID getId() {
        return id;
    }
}
