package entity.player;

import physics.Hitbox;
import util.Point2D;

public class PlayerHitbox implements Hitbox {

    private final Player player;

    public PlayerHitbox(Player player) {
        this.player = player;
    }

    @Override
    public float getRadius() {
        return PlayerConfig.HITBOX_RADIUS;
    }

    @Override
    public Point2D getPosition() {
        return player.getPosition();
    }

    @Override
    public void setPosition(Point2D resolvedPosition) {
        player.setPosition(resolvedPosition);
    }
}
