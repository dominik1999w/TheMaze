package entity.player;

import java.util.UUID;

import entity.WorldEntity;
import map.config.MapConfig;
import util.Point2D;

public class Player implements WorldEntity {
    private final UUID id = UUID.randomUUID();

    private final Point2D position = new Point2D();
    private float rotation = 0;

    public Player() {

    }

    public Player(Point2D position) {
        this.position.set(position);
        this.position.multiply(MapConfig.BOX_SIZE);
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
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
