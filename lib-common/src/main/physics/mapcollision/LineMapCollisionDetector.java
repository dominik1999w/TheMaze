package physics.mapcollision;

import java.util.ArrayList;
import java.util.List;
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
                    System.out.format("With (%s,%s): Bullet %s collision at (%f,%f)",
                            currentPosition, targetPosition, xWallType, x, y);
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
                    System.out.format("With (%s,%s): Bullet %s collision at (%f,%f)",
                            currentPosition, targetPosition, yWallType, x, y);
                    return new MapCollisionInfo(new Point2D(floor(x), y), true);
                }
                y += yStep;
                x += dxdy;
            }
        }

        return new MapCollisionInfo(targetPosition, false);
    }

    public void fun(Point2D playerPosition, float viewRadius, int numberOfRays) {
        List<Point2D> maxRange = new ArrayList<Point2D>();
        for(float i = 0; i<360; i+=(360.0/numberOfRays)){
            maxRange.add(detectLightingMapCollisions(playerPosition, i,viewRadius));
        }

        List<float[]> triangles = new ArrayList<float[]>();
        for(int i=0; i<numberOfRays; i++){
            float[] arr = { playerPosition.x(), playerPosition.y(),
                            maxRange.get(i).x(), maxRange.get(i).y(),
                            maxRange.get((i+1)%numberOfRays).x(), maxRange.get((i+1)%numberOfRays).y()};
            triangles.add(arr);
        }
    }

    public Point2D detectLightingMapCollisions(Point2D currentPosition, float angle/*deg*/, float radius) {
        angle = (float)Math.toRadians(angle);
        Point2D targetPosition = new Point2D(radius * (float)Math.cos(angle), radius * (float)Math.sin(angle));
        return detectLightingMapCollisions(currentPosition, targetPosition);
    }

    public Point2D detectLightingMapCollisions(Point2D currentPosition, Point2D deltaPosition) {
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
                    System.out.format("With (%s,%s): Light ray %s collision at (%f,%f)",
                            currentPosition, targetPosition, xWallType, x, y);
                    return new Point2D(x, y);
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
                    System.out.format("With (%s,%s): Light ray %s collision at (%f,%f)",
                            currentPosition, targetPosition, yWallType, x, y);
                    return new Point2D(x, y);
                }
                y += yStep;
                x += dxdy;
            }
        }

        return new Point2D(targetPosition);
    }
}
