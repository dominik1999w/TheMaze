package physics.entitycollision;

import physics.HitboxHistory;
import util.Point2D;

public interface EntityCollisionDetector {
    default EntityCollisionInfo detectCollision(HitboxHistory<?> historyA, HitboxHistory<?> historyB) {
        return detectCollision(historyA, historyB.getPreviousPosition(), historyB.getHitbox().getPosition(), historyB.getHitbox().getRadius());
    }

    EntityCollisionInfo detectCollision(HitboxHistory<?> historyA, Point2D currentPositionB, Point2D targetPositionB, float radiusB);

    final class EntityCollisionInfo {
        public final float timePoint1;
        public final float timePoint2;
        public final boolean haveCollided;
        protected EntityCollisionInfo(float timePoint1, float timePoint2) {
            this.timePoint1 = timePoint1;
            this.timePoint2 = timePoint2;
            this.haveCollided = true;
        }
        protected EntityCollisionInfo(float timePoint) {
            this(timePoint, timePoint);
        }
        protected EntityCollisionInfo() {
            this.timePoint1 = 0;
            this.timePoint2 = 0;
            this.haveCollided = false;
        }
    }
}