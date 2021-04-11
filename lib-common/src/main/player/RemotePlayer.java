package player;

import util.Point2D;

public class RemotePlayer {

    private final Point2D position = new Point2D();
    private float rotation;

    public RemotePlayer() {

    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public float getX() { return this.position.x(); }

    public float getY() { return this.position.y(); }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getRotation() {
        return this.rotation;
    }
}
