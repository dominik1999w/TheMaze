package player;

public class RemotePlayer {

    private float x;
    private float y;
    private float rotation;

    public RemotePlayer() {

    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() { return this.x; }

    public float getY() { return this.y; }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getRotation() {
        return this.rotation;
    }
}
