package physics.mapcollision;

import map.Map;
import physics.HitboxHistory;
import util.Point2D;

public abstract class MapCollisionDetector {

    protected final Map map;

    protected MapCollisionDetector(Map map) {
        this.map = map;
    }

    public abstract MapCollisionInfo detectMapCollision(HitboxHistory<?> history);

    public static class MapCollisionInfo {
        public final Point2D nextPosition;
        public final boolean hasCollided;
        protected MapCollisionInfo(Point2D nextPosition, boolean hasCollided) {
            this.nextPosition = nextPosition;
            this.hasCollided = hasCollided;
        }
    }
}
