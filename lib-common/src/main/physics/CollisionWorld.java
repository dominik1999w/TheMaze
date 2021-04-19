package physics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.UUID;

import map.Map;
import physics.mapcollision.ClampMapCollisionDetector;
import physics.mapcollision.LineMapCollisionDetector;
import physics.mapcollision.MapCollisionDetector;
import util.Point2D;

public class CollisionWorld {

    private final Map map;
    private final Collection<HitboxHistory> hitboxHistories = new ArrayList<>();

    private final EnumMap<HitboxType, MapCollisionDetector> mapCollisionDetector = new EnumMap<>(HitboxType.class);

    public CollisionWorld(Map map) {
        this.map = map;
        mapCollisionDetector.put(HitboxType.SLOW, new ClampMapCollisionDetector(map));
        mapCollisionDetector.put(HitboxType.FAST, new LineMapCollisionDetector(map));
    }

    public void addHitbox(Hitbox hitbox) {
        hitboxHistories.add(new HitboxHistory(hitbox, new Point2D(hitbox.getPosition())));
    }

    public void update() {
        Collection<Hitbox> hitboxesToNotify = new ArrayList<>();
        for (HitboxHistory hitboxHistory : hitboxHistories) {
            MapCollisionDetector.MapCollisionInfo collisionInfo = mapCollisionDetector
                    .get(hitboxHistory.getHitbox().getType())
                    .detectMapCollision(hitboxHistory);
            if (collisionInfo.hasCollided) {
                hitboxHistory.getHitbox().setPosition(collisionInfo.nextPosition);
                hitboxesToNotify.add(hitboxHistory.getHitbox());
            }

            hitboxHistory.setPreviousPosition(collisionInfo.nextPosition);
        }

        hitboxesToNotify.forEach(Hitbox::notifyMapCollision);
    }

    public void removeHitbox(UUID hitboxID) {
        hitboxHistories.removeIf(hitboxHistory ->hitboxHistory.getHitbox().getId().equals(hitboxID));
    }
}
