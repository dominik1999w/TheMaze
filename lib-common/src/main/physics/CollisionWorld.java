package physics;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import physics.mapcollision.ClampMapCollisionDetector;
import physics.mapcollision.LineMapCollisionDetector;
import physics.mapcollision.MapCollisionDetector;

public class CollisionWorld {

    private final Map<UUID, HitboxHistory> hitboxHistories = new HashMap<>();
    private HitboxHistory bulletHistory = null;

    private final EnumMap<HitboxType, MapCollisionDetector> mapCollisionDetector = new EnumMap<>(HitboxType.class);

    public CollisionWorld(map.Map map) {
        mapCollisionDetector.put(HitboxType.SLOW, new ClampMapCollisionDetector(map));
        mapCollisionDetector.put(HitboxType.FAST, new LineMapCollisionDetector(map));
    }

    public void addHitbox(Hitbox hitbox) {
        if (hitbox.getType() == HitboxType.FAST) {
            bulletHistory = new HitboxHistory(hitbox);
        } else {
            hitboxHistories.put(hitbox.getId(), new HitboxHistory(hitbox));
        }
    }

    public void onPlayerMoved(UUID id) {
        HitboxHistory hitboxHistory = hitboxHistories.get(id);
        if (hitboxHistory == null) {
            System.out.println("TRACE: onPlayerMoved on not existing player");
            return;
        }

        onHitboxMoved(hitboxHistory);
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

    private void onHitboxMoved(HitboxHistory hitboxHistory) {
        MapCollisionDetector.MapCollisionInfo collisionInfo = mapCollisionDetector
                .get(hitboxHistory.getHitbox().getType())
                .detectMapCollision(hitboxHistory);
        hitboxHistory.setPreviousPosition(collisionInfo.nextPosition);
        if (collisionInfo.hasCollided) {
            hitboxHistory.getHitbox().notifyMapCollision(collisionInfo.nextPosition);
        }
    }
}
