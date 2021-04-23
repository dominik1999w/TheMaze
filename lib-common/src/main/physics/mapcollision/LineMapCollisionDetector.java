package physics.mapcollision;

import java.util.Locale;

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

    public MapCollisionInfo detectMapCollisions(Point2D currentPosition, Point2D deltaPosition, float hitboxRadius) {
        Point2D targetPosition = new Point2D(currentPosition).add(deltaPosition);

        Point2Di currentTile = new Point2Di(
                floor(currentPosition.x() / MapConfig.BOX_SIZE),
                floor(currentPosition.y() / MapConfig.BOX_SIZE)
        );
        Point2Di targetTile = new Point2Di(
                floor(targetPosition.x() / MapConfig.BOX_SIZE),
                floor(targetPosition.y() / MapConfig.BOX_SIZE)
        );

        int xStep = 1, yStep = 1;
        float dy = (targetPosition.y() - currentPosition.y());
        float dx = (targetPosition.x() - currentPosition.x());
        WallType yWallType = WallType.UP_WALL;
        WallType xWallType = WallType.RIGHT_WALL;
        Point2Di tileAreaSize = new Point2Di(targetTile).subtract(currentTile);
        if (tileAreaSize.y() < 0) {
            yStep = -1;
            tileAreaSize.set(tileAreaSize.x(), -tileAreaSize.y());
            yWallType = WallType.DOWN_WALL;
        }
        if (tileAreaSize.x() < 0) {
            xStep = -1;
            tileAreaSize.set(-tileAreaSize.x(), tileAreaSize.y());
            xWallType = WallType.LEFT_WALL;
        }

        float x, y;
        if (Math.abs(dx) > Float.MIN_VALUE) {
            x = currentTile.x();
            y = currentTile.y();
            float dydx = dy / dx;
            for (int i = 0; i < tileAreaSize.x(); i++) {
                if (map.hasWall(xWallType, (int) x, floor(y))) {
                    System.out.println(String.format(Locale.ENGLISH,
                            "With (%s,%s): Bullet %s collision at (%f,%f)",
                            currentPosition, targetPosition, xWallType, x, y));
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
            for (int i = 0; i < tileAreaSize.y(); i++) {
                if (map.hasWall(yWallType, floor(x), (int) y)) {
                    System.out.println(String.format(Locale.ENGLISH,
                            "With (%s,%s): Bullet %s collision at (%f,%f)",
                            currentPosition, targetPosition, yWallType, x, y));
                    return new MapCollisionInfo(new Point2D(floor(x), y), true);
                }
                y += yStep;
                x += dxdy;
            }
        }

        return new MapCollisionInfo(targetPosition, false);
    }
}
