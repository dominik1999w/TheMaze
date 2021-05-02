package entity.player.controller;

import entity.player.Player;
import world.World;

public class LocalPlayerController extends InputPlayerController implements GameAuthoritativeListener {

    public LocalPlayerController(Player player, World<?> world) {
        super(player, world);
    }

    @Override
    public void setNextState(long timestamp, Player player) {
        // SPAM
        //System.out.print("Client: " + this.player.getPosition());
        //System.out.println("    Server: " + player.getPosition());

        this.player.setPosition(player.getPosition());
        this.player.setRotation(player.getRotation());
    }
}
