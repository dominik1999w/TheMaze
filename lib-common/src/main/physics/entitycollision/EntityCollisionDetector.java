package physics.entitycollision;

import physics.HitboxHistory;
import util.Point2D;

public interface EntityCollisionDetector {
    default EntityCollisionInfo detectCollision(HitboxHistory<?> historyA, HitboxHistory<?> historyB) {
        return detectCollision(historyA, historyB.getPreviousPosition(), historyB.getHitbox().getPosition(), historyB.getHitbox().getRadius());
    }

    EntityCollisionInfo detectCollision(HitboxHistory<?> historyA, Point2D currentPositionB, Point2D targetPositionB, float radiusB);

    final class EntityCollisionInfo {
        public final float timePoint;
        public final boolean haveCollided;
        protected EntityCollisionInfo(float timePoint) {
            this.timePoint = timePoint;
            this.haveCollided = true;
        }
        protected EntityCollisionInfo() {
            this.timePoint = 0;
            this.haveCollided = false;
        }
    }
}