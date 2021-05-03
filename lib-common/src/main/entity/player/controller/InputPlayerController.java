package entity.player.controller;

import entity.player.Player;
import entity.player.PlayerConfig;
import entity.player.PlayerInput;
import map.MapConfig;
import util.Point2D;
import world.World;

public class InputPlayerController extends PlayerController {

    private final World<?> world;
    private final PlayerInput playerInput;

    public InputPlayerController(Player player, World<?> world) {
        super(player);
        this.playerInput = new PlayerInput();
        this.world = world;
    }

    public void updateInput(PlayerInput playerInput) {
        this.playerInput.set(playerInput);
    }

    public void update() {
        if (playerInput.isEmpty()) return;

        if (playerInput.isShootPressed()) {
            world.onBulletFired(player);
        }

        Point2D deltaPosition = new Point2D(playerInput.getX(), playerInput.getY())
                .multiply(MapConfig.BOX_SIZE * PlayerConfig.INITIAL_SPEED * playerInput.getDelta());

        player.getPosition().add(deltaPosition);

        if (playerInput.getX() != 0 || playerInput.getY() != 0) {
            player.setRotation((float) Math.toDegrees(Math.atan2(playerInput.getY(), playerInput.getX())));
        }

        playerInput.clear();
    }
}