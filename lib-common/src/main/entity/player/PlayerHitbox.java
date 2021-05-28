package entity.player;

import java.util.UUID;

import physics.Hitbox;
import physics.HitboxType;
import util.Point2D;

public class PlayerHitbox implements Hitbox {

    private final Player player;

    public PlayerHitbox(Player player) {
        this.player = player;
    }

    @Override
    public UUID getId() {
        return player.getId();
    }

    @Override
    public HitboxType getType() {
        return HitboxType.SLOW;
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
    public void notifyMapCollision(Point2D resolvedPosition) {
        player.setPosition(resolvedPosition);
    }

    @Override
    public void notifyEntityCollision(Hitbox hitbox) {
        // TODO: player was hit by a bullet
    }
}
