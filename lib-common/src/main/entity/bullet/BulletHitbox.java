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
    public void setPosition(Point2D resolvedPosition) {
        bullet.setPosition(resolvedPosition);
    }

    @Override
    public void notifyMapCollision() {
        world.onBulletDied(bullet.getId());
    }
}
