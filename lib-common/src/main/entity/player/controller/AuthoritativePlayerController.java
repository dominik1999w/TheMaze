package entity.player.controller;

import entity.player.Player;
import util.Point2D;
import world.World;

public class AuthoritativePlayerController extends PlayerController {

    private final World<?> world;

    private final Point2D nextPosition = new Point2D();
    private float nextRotation = 0;
    private boolean nextFireBullet;

    public AuthoritativePlayerController(Player player, World<?> world) {
        super(player);
        this.world = world;
    }

    @Override
    public void update(float delta) {
        player.setPosition(nextPosition);
        player.setRotation(nextRotation);
        if(nextFireBullet) {
            world.onBulletFired(player);
            //world.onBulletDied(world.getBulletController(player).getBulletId());
        }
    }

    public void setNextPosition(float positionX, float positionY) {
        nextPosition.set(positionX, positionY);
    }

    public void setNextRotation(float rotation) {
        this.nextRotation = rotation;
    }

    public void setNextFireBullet(boolean fireBullet) {
        nextFireBullet = fireBullet;
    }
}
