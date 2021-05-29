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

    private final EnumMap<HitboxType, MapCollisionDetector> mapCollisionDetector = new EnumMap<>(HitboxType.class);
    private final EntityCollisionDetector entityCollisionDetector = new SimpleEntityCollisionDetector();

    public CollisionWorld(map.Map map) {
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
            if (moveEndTimestamp >= bulletHistory.getHitbox().getBirthTimestamp()) {
                Point2D bulletCurrentPosition = bulletHistory.getHitbox().getPosition(moveTimestamp);
                Point2D bulletTargetPosition = bulletHistory.getHitbox().getPosition(moveEndTimestamp);
                System.out.format("%s, %s, %s, %f, %s, %s, %s, %s\n", System.currentTimeMillis(),
                        moveTimestamp, (moveTimestamp + (long) (deltaTime * 1000.0f)), deltaTime,
                        hitboxHistory.getPreviousPosition(), hitboxHistory.getHitbox().getPosition(),
                        bulletCurrentPosition, bulletTargetPosition);

                EntityCollisionDetector.EntityCollisionInfo collisionInfo = entityCollisionDetector.detectCollision(hitboxHistory,
                        bulletCurrentPosition, bulletTargetPosition, bulletHistory.getHitbox().getRadius());

                if (collisionInfo.haveCollided) {
                    long collisionTimestamp = moveTimestamp + (long) (deltaTime * 1000.0f * collisionInfo.timePoint);
                    if (collisionTimestamp >= bulletHistory.getHitbox().getBirthTimestamp()) {
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
        if (bulletHistory != null) {
            onHitboxMoved(bulletHistory);
        }
    }

    public void removeHitbox(UUID hitboxID) {
        if (bulletHistory != null && bulletHistory.getHitbox().getId().equals(hitboxID)) {
            bulletHistory = null;
        } else {
            hitboxHistories.remove(hitboxID);
        }
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
}
