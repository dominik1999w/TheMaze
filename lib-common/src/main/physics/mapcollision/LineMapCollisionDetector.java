package physics.mapcollision;

import map.Map;
import map.MapConfig;
import physics.HitboxHistory;
import types.WallType;
import util.Point2D;
import util.Point2Di;

import static util.MathUtils.floor;

public class LineMapCollisionDetector extends MapCollisionDetector {

    public LineMapCollisionDetector(Map map) {
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
                floor(initialPosition.x() / MapConfig.BOX_SIZE),
                floor(initialPosition.y() / MapConfig.BOX_SIZE)
        );
        Point2Di targetTile = new Point2Di(
                floor(targetPosition.x() / MapConfig.BOX_SIZE),
                floor(targetPosition.y() / MapConfig.BOX_SIZE)
        );

        int xStep = 1, yStep = 1;
        float dy = (targetPosition.y() - currentPosition.y());
        float dx = (targetPosition.x() - currentPosition.x());
        int xFirstIndex = 1, yFirstIndex = 1;
        Point2Di tileAreaSize = new Point2Di(targetTile).subtract(currentTile);
        if (tileAreaSize.y() < 0) {
            yStep = -1;
            tileAreaSize.set(tileAreaSize.x(), -tileAreaSize.y());
            yFirstIndex = 0;
        }
        if (tileAreaSize.x() < 0) {
            xStep = -1;
            tileAreaSize.set(-tileAreaSize.x(), tileAreaSize.y());
            xFirstIndex = 0;
        }

        float x, y;
        if (Math.abs(dx) > Float.MIN_VALUE) {
            x = currentTile.x();
            y = currentTile.y();
            float dydx = dy / dx;
            for (int i = xFirstIndex; i <= xFirstIndex + tileAreaSize.x(); i++) {
                if (map.hasWall(WallType.LEFT_WALL, (int) x, floor(y))) {
                    System.out.println("Bullet LEFT_WALL collision at " + x + "," + y);
                    return new MapCollisionInfo(new Point2D(x, floor(y)), true);
                }
                x += xStep;
                y += dydx;
            }
        }
        if (Math.abs(dy) > Float.MIN_VALUE) {
            x = currentTile.x();
            y = currentTile.y();
            float dxdy = dx / dy;
            for (int i = yFirstIndex; i <= yFirstIndex + tileAreaSize.y(); i++) {
                if (map.hasWall(WallType.DOWN_WALL, floor(x), (int) y)) {
                    System.out.println("Bullet DOWN_WALL collision at " + x + "," + y);
                    return new MapCollisionInfo(new Point2D(floor(x), y), true);
                }
                y += yStep;
                x += dxdy;
            }
        }

        return new MapCollisionInfo(targetPosition, false);
    }
}
