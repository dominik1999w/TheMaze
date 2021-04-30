package entity.player.controller;

import java.util.ArrayDeque;
import java.util.Queue;

import entity.player.GameInputListener;
import entity.player.Player;
import entity.player.PlayerConfig;
import entity.player.PlayerInput;
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

    @Override
    public void update(float delta) {
        while (!inputQueue.isEmpty()) {
            PlayerInput playerInput = inputQueue.poll();

            if (bulletTimeout <= 0 && playerInput.isShootPressed()) {
                world.onBulletFired(player);
                bulletTimeout = 1;
            }

            Point2D deltaPosition = new Point2D(playerInput.getX(), playerInput.getY())
                    .multiply(MapConfig.BOX_SIZE * PlayerConfig.INITIAL_SPEED * playerInput.getDelta());

            player.getPosition().add(deltaPosition);

            if (playerInput.getX() != 0 || playerInput.getY() != 0) {
                player.setRotation((float) Math.toDegrees(Math.atan2(playerInput.getY(), playerInput.getX())));
            }
        }
    }

    private final Queue<PlayerInput> inputQueue = new ArrayDeque<>();

    @Override
    public void notifyInput(PlayerInput playerInput) {
        inputQueue.add(playerInput);
    }
}