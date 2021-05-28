package physics.entitycollision;

import physics.HitboxHistory;
import util.Point2D;

public interface EntityCollisionDetector {
    default boolean detectCollision(HitboxHistory<?> historyA, HitboxHistory<?> historyB) {
        return detectCollision(historyA, historyB.getPreviousPosition(), historyB.getHitbox().getPosition(), historyB.getHitbox().getRadius());
    }

    boolean detectCollision(HitboxHistory<?> historyA, Point2D currentPositionB, Point2D targetPositionB, float radiusB);
}