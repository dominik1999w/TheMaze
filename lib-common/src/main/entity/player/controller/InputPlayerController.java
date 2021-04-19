package entity.player.controller;

import entity.player.Player;
import entity.player.PlayerConfig;
import input.IPlayerInput;
import map.MapConfig;
import util.Point2D;
import world.World;

public class InputPlayerController extends PlayerController {

    private final IPlayerInput playerInput;
    private final World<?> world;

    private float bulletTimeout = 0;

    public InputPlayerController(Player player, IPlayerInput playerInput, World<?> world) {
        super(player);
        this.playerInput = playerInput;
        this.world = world;
    }

    public void update(float delta) {
        if (bulletTimeout <= 0 && playerInput.isShootPressed()) {
            world.onBulletFired(player);
            bulletTimeout = 1;
        }
        if (bulletTimeout > 0) {
            bulletTimeout -= delta;
        }

        Point2D deltaPosition = new Point2D(
                playerInput.getX(),
                playerInput.getY()
        ).multiply(MapConfig.BOX_SIZE * PlayerConfig.INITIAL_SPEED * delta);

        player.getPosition().add(deltaPosition);

        if (playerInput.getX() != 0 || playerInput.getY() != 0) {
            player.setRotation((float)Math.toDegrees(Math.atan2(playerInput.getY(), playerInput.getX())));
        }
    }
}