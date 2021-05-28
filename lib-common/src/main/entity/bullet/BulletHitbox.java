package entity.bullet;

import java.util.UUID;

import physics.Hitbox;
import physics.HitboxType;
import util.Point2D;
import world.World;

public class BulletHitbox implements Hitbox {

    private final Bullet bullet;
    private final World<?> world;

    public BulletHitbox(Bullet bullet, World<?> world) {
        this.bullet = bullet;
        this.world = world;
    }

    @Override
    public UUID getId() {
        return bullet.getId();
    }

    @Override
    public HitboxType getType() {
        return HitboxType.FAST;
    }

    @Override
    public float getRadius() {
        return BulletConfig.HITBOX_RADIUS;
    }

    @Override
    public Point2D getPosition() {
        return bullet.getPosition();
    }

    @Override
    public void notifyMapCollision(Point2D resolvedPosition) {
        world.onBulletDied();
    }

    @Override
    public void notifyEntityCollision(Hitbox hitbox) {
        // TODO: if hitbox.getId() != bullet.shooterID
        world.onBulletDied();
    }
}
