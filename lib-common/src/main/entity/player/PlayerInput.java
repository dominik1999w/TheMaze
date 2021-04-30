package entity.player;

public class PlayerInput {

    private float delta;

    private final float x;
    private final float y;
    private final boolean shootPressed;

    public PlayerInput(float delta, float x, float y, boolean shootPressed) {
        this.delta = delta;
        this.x = x;
        this.y = y;
        this.shootPressed = shootPressed;
    }

    public PlayerInput(float x, float y, boolean shootPressed) {
        this(0, x, y, shootPressed);
    }

    public float getDelta() {
        return delta;
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isShootPressed() {
        return shootPressed;
    }
}
