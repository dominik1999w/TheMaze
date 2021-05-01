package entity.player.controller;

import entity.player.Player;
import util.Point2D;
import world.World;

public class AuthoritativePlayerController extends PlayerController implements GameAuthoritativeListener {

    private final World<?> world;

    private final Point2D nextPosition = new Point2D();
    private float nextRotation = 0;
    private boolean nextFireBullet;

    public AuthoritativePlayerController(Player player, World<?> world) {
        super(player);
        this.world = world;
    }

    public void update() {
        player.setPosition(nextPosition);
        player.setRotation(nextRotation);
        if (nextFireBullet) {
            nextFireBullet = false;
            world.onBulletFired(player);
            //world.onBulletDied(world.getBulletController(player).getBulletId());
        }
    }

    // NOTE: probably not the best way to represent state
    @Override
    public void setNextState(Player player) {
        this.nextPosition.set(player.getPosition());
        this.nextRotation = player.getRotation();
    }

//    public void setNextFireBullet(boolean fireBullet) {
//        nextFireBullet = fireBullet;
//    }
}
