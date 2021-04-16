package entity.player.controller;

import entity.player.Player;
import entity.player.PlayerConfig;
import input.IPlayerInput;
import map.CollisionFinder;
import map.Map;
import map.config.MapConfig;
import util.Point2D;
import world.World;

public class InputPlayerController extends PlayerController {

    private final IPlayerInput playerInput;
    private final CollisionFinder collisionFinder;
    private final World<?> world;

    public InputPlayerController(Player player, IPlayerInput playerInput, Map map, World<?> world) {
        super(player);
        this.playerInput = playerInput;
        this.collisionFinder = new CollisionFinder(map, PlayerConfig.HITBOX_RADIUS);
        this.world = world;
    }

    public void update(float delta) {
        if (playerInput.isShootPressed()) world.onBulletFired(player);

        Point2D deltaPosition = new Point2D(
                playerInput.getX(),
                playerInput.getY()
        ).multiply(MapConfig.BOX_SIZE * PlayerConfig.INITIAL_SPEED * delta);

        player.setPosition(collisionFinder.getNewPosition(player.getPosition(), deltaPosition));

        if (playerInput.getX() != 0 || playerInput.getY() != 0) {
            player.setRotation((float)Math.toDegrees(Math.atan2(playerInput.getY(), playerInput.getX())));
        }
    }
}