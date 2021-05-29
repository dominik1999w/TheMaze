package physics;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import entity.bullet.BulletHitbox;
import physics.entitycollision.EntityCollisionDetector;
import physics.entitycollision.SimpleEntityCollisionDetector;
import physics.mapcollision.ClampMapCollisionDetector;
import physics.mapcollision.LineMapCollisionDetector;
import physics.mapcollision.MapCollisionDetector;
import util.Point2D;

public class CollisionWorld {

    private final Map<UUID, HitboxHistory<?>> hitboxHistories = new HashMap<>();

    private HitboxHistory<BulletHitbox> bulletHistory = null;
    private long bulletDeathTimestamp;

    private final EnumMap<HitboxType, MapCollisionDetector> mapCollisionDetector = new EnumMap<>(HitboxType.class);
    private final EntityCollisionDetector entityCollisionDetector = new SimpleEntityCollisionDetector();

    public CollisionWorld(map.Map map) {
        this.bulletDeathTimestamp = System.currentTimeMillis();
        mapCollisionDetector.put(HitboxType.SLOW, new ClampMapCollisionDetector(map));
        mapCollisionDetector.put(HitboxType.FAST, new LineMapCollisionDetector(map));
    }

    public void addPlayerHitbox(Hitbox hitbox) {
        hitboxHistories.put(hitbox.getId(), new HitboxHistory<>(hitbox));
    }

    public void setBulletHitbox(BulletHitbox hitbox) {
        bulletHistory = new HitboxHistory<>(hitbox);
    }

    public void onPlayerMoved(UUID id, long moveTimestamp, float deltaTime) {
        HitboxHistory<?> hitboxHistory = hitboxHistories.get(id);
        if (hitboxHistory == null) {
            System.out.println("TRACE: onPlayerMoved on not existing player");
            return;
        }

        if (bulletHistory != null) {
            // Player: hitboxHistory.getPreviousPosition() -> hitboxHistory.getHitbox().getPosition()
            // Bullet: bulletHistory.getPosition(timestamp) -> bulletHistory.getPosition(timestamp + deltaTime)
            long moveEndTimestamp = moveTimestamp + (long)(deltaTime * 1000.0f);
            if (moveEndTimestamp >= bulletHistory.getHitbox().getBirthTimestamp() && (bulletIsActive() || moveTimestamp <= bulletDeathTimestamp)) {
                Point2D bulletCurrentPosition = bulletHistory.getHitbox().getPosition(moveTimestamp);
                Point2D bulletTargetPosition = bulletHistory.getHitbox().getPosition(moveEndTimestamp);
                System.out.format("%s, %s, %s, %s, %s\n", System.currentTimeMillis(),
                        moveTimestamp, moveEndTimestamp, bulletHistory.getHitbox().getBirthTimestamp(), bulletDeathTimestamp);

                EntityCollisionDetector.EntityCollisionInfo collisionInfo = entityCollisionDetector.detectCollision(hitboxHistory,
                        bulletCurrentPosition, bulletTargetPosition, bulletHistory.getHitbox().getRadius());

                if (collisionInfo.haveCollided) {
                    long collisionTimestamp1 = moveTimestamp + (long) (deltaTime * 1000.0f * collisionInfo.timePoint1);
                    long collisionTimestamp2 = moveTimestamp + (long) (deltaTime * 1000.0f * collisionInfo.timePoint2);
                    if (bulletHistory.getHitbox().getBirthTimestamp() <= collisionTimestamp1 && collisionTimestamp1 <= bulletDeathTimestamp) {
                        hitboxHistory.getHitbox().notifyEntityCollision(bulletHistory.getHitbox());
                        bulletHistory.getHitbox().notifyEntityCollision(hitboxHistory.getHitbox());
                    } else if (bulletHistory.getHitbox().getBirthTimestamp() <= collisionTimestamp2 && collisionTimestamp2 <= bulletDeathTimestamp) {
                        hitboxHistory.getHitbox().notifyEntityCollision(bulletHistory.getHitbox());
                        bulletHistory.getHitbox().notifyEntityCollision(hitboxHistory.getHitbox());
                    }
                }
            }
        }

        if (hitboxHistory != null) {
            onHitboxMoved(hitboxHistory);
        }
    }

    public void update() {
        if (bulletIsActive()) {
            onHitboxMoved(bulletHistory);
        }
    }

    public void removeBulletHitbox(UUID hitboxID) {
        if (bulletHistory.getHitbox().getId().equals(hitboxID)) {
            bulletDeathTimestamp = System.currentTimeMillis();
        }
    }

    public void removePlayerHitbox(UUID hitboxID) {
        hitboxHistories.remove(hitboxID);
    }

    private void onHitboxMoved(HitboxHistory<?> hitboxHistory) {
        MapCollisionDetector.MapCollisionInfo collisionInfo = mapCollisionDetector
                .get(hitboxHistory.getHitbox().getType())
                .detectMapCollision(hitboxHistory);
        hitboxHistory.setPreviousPosition(collisionInfo.nextPosition);
        if (collisionInfo.hasCollided) {
            hitboxHistory.getHitbox().notifyMapCollision(collisionInfo.nextPosition);
        }
    }

    private boolean bulletIsActive() {
        return bulletHistory != null && bulletHistory.getHitbox().getBirthTimestamp() > bulletDeathTimestamp;
    }
}
