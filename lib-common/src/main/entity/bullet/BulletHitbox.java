package entity.bullet;

import java.util.UUID;

import map.MapConfig;
import physics.Hitbox;
import physics.HitboxType;
import util.Point2D;
import world.RoundResult;
import world.World;

public class BulletHitbox implements Hitbox {

    private final UUID shooterID;
    private final Bullet bullet;
    private final World<?> world;

    private final Point2D bulletVelocity;
    private final Point2D bulletStartPosition;
    private final long startTimestamp;

    public BulletHitbox(UUID shooterID, Bullet bullet, World<?> world) {
        this.shooterID = shooterID;
        this.bullet = bullet;
        this.world = world;

        this.bulletVelocity = new Point2D(
                (float) Math.cos(Math.toRadians(bullet.getRotation())),
                (float) Math.sin(Math.toRadians(bullet.getRotation()))
        ).multiply(MapConfig.BOX_SIZE * bullet.getSpeed());
        this.bulletStartPosition = new Point2D(bullet.getPosition());
        this.startTimestamp = System.currentTimeMillis();

        System.out.format("Bullet velocity: %s\n", bulletVelocity);
        System.out.format("Bullet start position: %s\n", bulletStartPosition);
        System.out.format("Start timestamp: %s\n", startTimestamp);
    }

    @Override
    public UUID getId() {
        return bullet.getId();
    }

    public UUID getShooterID() {
        return shooterID;
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

    public Point2D getPosition(long timestamp) {
        return new Point2D(bulletVelocity).multiply((timestamp - startTimestamp) / 1000.0f)
                .add(bulletStartPosition);
    }

    @Override
    public void notifyMapCollision(Point2D resolvedPosition) {
        System.out.format("Bullet_MapCollision: %s\n", resolvedPosition);
        bullet.setPosition(resolvedPosition);
        world.onBulletDiedDelayed();
        world.endRound(RoundResult.missed(shooterID));
    }

    @Override
    public void notifyEntityCollision(Hitbox hitbox) {
        System.out.format("Bullet_EntityCollision: %s\n", getPosition());
        if (!shooterID.equals(hitbox.getId())) {
            System.out.format("Bullet_EntityCollision: %s\n", getPosition());
            world.onBulletDied();
            world.endRound(RoundResult.killed(shooterID, hitbox.getId()));
        }
    }

    public long getBirthTimestamp() {
        return startTimestamp;
    }
}
