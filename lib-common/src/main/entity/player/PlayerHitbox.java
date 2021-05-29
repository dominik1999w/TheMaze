package entity.player;

import java.util.UUID;

import entity.bullet.BulletHitbox;
import physics.Hitbox;
import physics.HitboxType;
import util.Point2D;
import world.World;

public class PlayerHitbox implements Hitbox {

    private final Player player;
    private final World<?> world;

    public PlayerHitbox(Player player, World<?> world) {
        this.player = player;
        this.world = world;
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
        if(hitbox instanceof BulletHitbox && ((BulletHitbox)hitbox).getShooterID() != getId()) {
            System.out.format("Player %s was hit by a bullet!\n", getId());
            if(!player.isKilled()) {
                player.kill();
                world.killPlayer(hitbox.getId(), player.getId());
            }
        }
    }
}
