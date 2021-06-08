package entity.player.controller;

import entity.player.Player;
import entity.player.PlayerInterpolator;
import world.World;

import static util.ServerConfig.SERVER_UPDATE_RATE;

public class AuthoritativePlayerController extends PlayerController implements GameAuthoritativeListener {

    private final PlayerInterpolator playerInterpolator;

    public AuthoritativePlayerController(Player player, World<?> world) {
        super(player);
        this.playerInterpolator = new PlayerInterpolator(SERVER_UPDATE_RATE);
    }

    public void update() {
        playerInterpolator.computeCurrentState(player);
    }

    // NOTE: probably not the best way to represent state
    @Override
    public void setNextState(long timestamp, Player playerState) {
        playerInterpolator.addState(timestamp, playerState);
    }
}
