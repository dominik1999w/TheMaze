package entity.player.controller;

import entity.player.Player;
import util.Point2D;

public class AuthoritativePlayerController extends PlayerController {

    private final Point2D nextPosition = new Point2D();
    private float nextRotation = 0;

    public AuthoritativePlayerController(Player player) {
        super(player);
    }

    @Override
    public void update(float delta) {
        player.setPosition(nextPosition);
        player.setRotation(nextRotation);
    }

    public void setNextPosition(float positionX, float positionY) {
        nextPosition.set(positionX, positionY);
    }

    public void setNextRotation(float rotation) {
        this.nextRotation = rotation;
    }
}
