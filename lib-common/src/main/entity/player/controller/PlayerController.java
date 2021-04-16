package entity.player.controller;

import entity.player.Player;
import util.Point2D;

public abstract class PlayerController {

    protected final Player player;

    protected PlayerController(Player player) {
        this.player = player;
    }

    public abstract void update(float delta);

    public final Point2D getPlayerPosition() {
        return player.getPosition();
    }

    public final float getPlayerRotation() {
        return player.getRotation();
    }
}
