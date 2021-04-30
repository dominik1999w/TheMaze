package entity.player;

import java.util.UUID;

import entity.WorldEntity;
import util.Point2D;

public class Player implements WorldEntity {
    private final UUID id;

    private final Point2D position = new Point2D();
    private float rotation = 0;

    public Player() {
        this.id = UUID.randomUUID();
    }

    public Player(UUID id, Point2D position) {
        this.id = id;
        this.position.set(position);
    }

    public void setPosition(Point2D position) {
        this.position.set(position);
    }

    public Point2D getPosition() {
        return this.position;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getRotation() {
        return this.rotation;
    }

    @Override
    public UUID getId() {
        return id;
    }
}
