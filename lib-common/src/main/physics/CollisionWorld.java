package physics;

import java.util.ArrayList;
import java.util.Collection;

import map.Map;
import util.Point2D;

public class CollisionWorld {

    private final Collection<HitboxHistory> hitboxHistories = new ArrayList<>();

    public CollisionWorld() {
    }

    public void addHitbox(Hitbox hitbox) {
        hitboxHistories.add(new HitboxHistory(hitbox, new Point2D()));
    }

    public void update() {
        for (HitboxHistory historyA : hitboxHistories) {
            for (HitboxHistory historyB : hitboxHistories) {
                // checkCollision(hitboxA, hitboxB);
            }
        }
    }
}
