package entity.player;

public class PlayerInput {

    private float delta;

    private float x;
    private float y;
    private boolean shootPressed;

    public PlayerInput() {
        this(0, 0, false);
    }

    public PlayerInput(float x, float y, boolean shootPressed) {
        this(0, x, y, shootPressed);
    }

    public PlayerInput(float delta, float x, float y, boolean shootPressed) {
        this.delta = delta;
        this.x = x;
        this.y = y;
        this.shootPressed = shootPressed;
    }

    public void set(PlayerInput other) {
        this.delta = other.delta;
        this.x = other.x;
        this.y = other.y;
        this.shootPressed = other.shootPressed;
    }

    public boolean isEmpty() {
        return x == 0 && y == 0 && !shootPressed;
    }

    public void clear() {
        this.x = this.y = 0;
        this.shootPressed = false;
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

    @Override
    public String toString() {
        return "PlayerInput{" +
                "delta=" + delta +
                ", x=" + x +
                ", y=" + y +
                ", shootPressed=" + shootPressed +
                '}';
    }
}
