package entity.bullet;

import java.util.UUID;

import entity.WorldEntity;
import util.Point2D;

public class Bullet implements WorldEntity {
    private final UUID id = UUID.randomUUID();

    private final Point2D position;
    private final float rotation;
    private final float speed;

    public Bullet(Point2D position, float angle) {
        this.position = new Point2D(position).add(BulletConfig.textureDependentShift(angle));

        this.rotation = angle;
        this.speed = BulletConfig.INITIAL_SPEED;
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
