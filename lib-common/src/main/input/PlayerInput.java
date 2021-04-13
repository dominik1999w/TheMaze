package input;

public class PlayerInput implements IPlayerInput {

    private float x = 0;
    private float y = 0;
    private boolean shootPressed = false;

    @Override
    public boolean isShootPressed() {
        return shootPressed;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setShootPressed(boolean shootPressed) {
        this.shootPressed = shootPressed;
    }
}
