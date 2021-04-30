package entity.player.controller;

import entity.player.GameAuthoritativeListener;
import entity.player.Player;
import world.World;

public class LocalPlayerController extends InputPlayerController implements GameAuthoritativeListener {

    public LocalPlayerController(Player player, World<?> world) {
        super(player, world);
    }

    @Override
    public void setNextState(Player player) {
        System.out.print("Client: " + this.player.getPosition());
        System.out.println("    Server: " + player.getPosition());
        // TODO: synchronize with reads in render()
        this.player.setPosition(player.getPosition());
        this.player.setRotation(player.getRotation());
    }
}
