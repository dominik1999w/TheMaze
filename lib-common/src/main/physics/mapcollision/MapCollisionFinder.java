package physics.mapcollision;

import map.Map;
import util.Point2D;

public abstract class MapCollisionFinder {

    protected final Map map;

    protected MapCollisionFinder(Map map) {
        this.map = map;
    }

    public abstract MapCollisionInfo getNewPosition(Point2D initial_position, Point2D delta_position, float hitboxRadius);

    public class MapCollisionInfo {
        public final Point2D nextPosition;
        public final boolean hasCollided;
        protected MapCollisionInfo(Point2D nextPosition, boolean hasCollided) {
            this.nextPosition = nextPosition;
            this.hasCollided = hasCollided;
        }
    }
}
