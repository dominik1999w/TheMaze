package physics.mapcollision;

import map.Map;
import map.MapConfig;
import physics.HitboxHistory;
import types.WallType;
import util.Point2D;
import util.Point2Di;

import static util.MathUtils.clamp;
import static util.MathUtils.floor;

public class ClampMapCollisionDetector extends MapCollisionDetector {
    public ClampMapCollisionDetector(Map map) {
        super(map);
    }

    @Override
    public MapCollisionInfo detectMapCollision(HitboxHistory history) {
        Point2D initialPosition = history.getPreviousPosition();
        Point2D deltaPosition = new Point2D(history.getHitbox().getPosition()).subtract(initialPosition);
        return detectMapCollisions(initialPosition, deltaPosition, history.getHitbox().getRadius());
    }

    public MapCollisionInfo detectMapCollisions(Point2D initialPosition, Point2D deltaPosition, float hitboxRadius) {
        Point2D currentPosition = new Point2D(initialPosition);
        Point2D targetPosition = new Point2D(initialPosition).add(deltaPosition);

        Point2Di currentTile = new Point2Di(
                floor(currentPosition.x() / MapConfig.BOX_SIZE),
                floor(currentPosition.y() / MapConfig.BOX_SIZE)
        );
        Point2Di targetTile = new Point2Di(
                floor(targetPosition.x() / MapConfig.BOX_SIZE),
                floor(targetPosition.y() / MapConfig.BOX_SIZE)
        );

        Point2Di collisionAreaMin = new Point2Di(currentTile)
                .min(targetTile)
                .subtract(new Point2Di(1, 1))
                .max(new Point2Di(0, 0));
        Point2Di collisionAreaMax = new Point2Di(currentTile)
                .max(targetTile)
                .add(new Point2Di(2, 2))
                .min(new Point2Di(MapConfig.MAP_LENGTH, MapConfig.MAP_LENGTH));

        boolean hasCollided = false;
        for (WallType.WallShape wallShape : map.getWallsInArea(collisionAreaMin, collisionAreaMax)) {
            Point2D distanceDirection = new Point2D(
                    clamp(targetPosition.x(), wallShape.getPositionX() * MapConfig.BOX_SIZE, wallShape.getPositionX() * MapConfig.BOX_SIZE + wallShape.getSizeX()),
                    clamp(targetPosition.y(), wallShape.getPositionY() * MapConfig.BOX_SIZE, wallShape.getPositionY() * MapConfig.BOX_SIZE + wallShape.getSizeY())
            ).subtract(targetPosition);
            float overlap = hitboxRadius * MapConfig.BOX_SIZE - distanceDirection.mag();
            if (Float.isNaN(overlap)) overlap = 0;
            if (overlap > 0) {
                hasCollided = true;
                targetPosition.subtract(distanceDirection.normalize().multiply(overlap));
            }
        }

        return new MapCollisionInfo(targetPosition, hasCollided);
    }
}
