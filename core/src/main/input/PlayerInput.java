package input;

public class PlayerInput implements IPlayerInput {

    private int x = 0;
    private int y = 0;
    private boolean shootPressed = false;

    @Override
    public boolean isShootPressed() {
        return shootPressed;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setShootPressed(boolean shootPressed) {
        this.shootPressed = shootPressed;
    }
}
