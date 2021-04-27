package entity.player.controller;

import entity.player.GameInputListener;
import entity.player.Player;
import entity.player.PlayerConfig;
import map.MapConfig;
import util.Point2D;
import world.World;

public class InputPlayerController extends PlayerController implements GameInputListener {

    private final World<?> world;

    private float bulletTimeout = 0;

    public InputPlayerController(Player player, World<?> world) {
        super(player);
        this.world = world;
    }

    public void update(float delta) {
        if (bulletTimeout <= 0 && isShootPressed) {
            world.onBulletFired(player);
            bulletTimeout = 1;
        }
        if (bulletTimeout > 0) {
            bulletTimeout -= delta;
        }

        Point2D deltaPosition = new Point2D(inputX, inputY)
                .multiply(MapConfig.BOX_SIZE * PlayerConfig.INITIAL_SPEED * delta);

        player.getPosition().add(deltaPosition);

        if (inputX != 0 || inputY != 0) {
            player.setRotation((float)Math.toDegrees(Math.atan2(inputY, inputX)));
        }
    }

    // NOTE: temporary
    private boolean isShootPressed = false;
    private float inputX = 0;
    private float inputY = 0;

    @Override
    public void notifyInput(float x, float y, boolean shootPressed) {
        this.inputX = x;
        this.inputY = y;
        this.isShootPressed = shootPressed;
    }
}