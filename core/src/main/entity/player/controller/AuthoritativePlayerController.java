package entity.player.controller;

import entity.player.Player;
import entity.player.PlayerInterpolator;
import world.World;

import static util.ServerConfig.SERVER_UPDATE_RATE;

public class AuthoritativePlayerController extends PlayerController implements GameAuthoritativeListener {

    private final World<?> world;

    private final PlayerInterpolator playerInterpolator;
    private boolean nextFireBullet;

    public AuthoritativePlayerController(Player player, World<?> world) {
        super(player);
        this.world = world;
        this.playerInterpolator = new PlayerInterpolator(SERVER_UPDATE_RATE);
    }

    public void update() {
        playerInterpolator.computeCurrentState(player);
        if (nextFireBullet) {
            nextFireBullet = false;
            world.onBulletFired(player);
            //world.onBulletDied(world.getBulletController(player).getBulletId());
        }
    }

    // NOTE: probably not the best way to represent state
    @Override
    public void setNextState(Player playerState, long timestamp) {
        playerInterpolator.addState(playerState, timestamp);
    }

//    public void setNextFireBullet(boolean fireBullet) {
//        nextFireBullet = fireBullet;
//    }
}
