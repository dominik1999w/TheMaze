package physics.mapcollision;

import java.util.ArrayList;
import java.util.List;

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
                    System.out.format("With (%s,%s): Bullet %s collision at (%f,%f)\n",
                            currentPosition, targetPosition, xWallType, x, y);
                    return new MapCollisionInfo(new Point2D(x, y), true);
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
                    System.out.format("With (%s,%s): Bullet %s collision at (%f,%f)\n",
                            currentPosition, targetPosition, yWallType, x, y);
                    return new MapCollisionInfo(new Point2D(x, y), true);
                }
                y += yStep;
                x += dxdy;
            }
        }

        return new MapCollisionInfo(targetPosition, false);
    }

    public List<float[]> getSightTriangles(Point2D playerPosition, float viewRadius, int numberOfRays) {
        List<Point2D> maxRange = new ArrayList<Point2D>();
        for (float i = 0; i < 360; i += (360.0 / numberOfRays)) {
            maxRange.add(detectLightingMapCollisions(playerPosition, i, viewRadius));
            //System.out.format("%f %f \n",maxRange.get(maxRange.size() - 1).x(), maxRange.get(maxRange.size() - 1).y());
        }

        List<float[]> sightTriangles = new ArrayList<>();
        for (int i = 0; i < numberOfRays; i++) {
            float[] arr = {playerPosition.x(), playerPosition.y(),
                    maxRange.get(i).x(), maxRange.get(i).y(),
                    maxRange.get((i + 1) % numberOfRays).x(), maxRange.get((i + 1) % numberOfRays).y()};
            sightTriangles.add(arr);
        }
        return sightTriangles;
    }

    private float updateDist(float nDist, Point2D nCollision, Point2D nCurPos, float x, float y) {
        float newDist = Point2D.dist(nCurPos, new Point2D(x, y));
        if (newDist < nDist) {
            nCollision.set(x, y);
            return newDist;
        }
        return nDist;
    }

    private Point2D detectLightingMapCollisions(Point2D currentPosition, double angle/*deg*/, float radius) {
        angle = angle - (int) (angle / 360) * 360;
        double radAngle = Math.toRadians(angle);
        int quadrant = (int) (angle / 90) + 1;

        Point2D nCurPos = new Point2D(currentPosition.x() / MapConfig.BOX_SIZE, currentPosition.y() / MapConfig.BOX_SIZE);
        float nRadius = radius / MapConfig.BOX_SIZE;
        Point2D nDelta = new Point2D((float) (nRadius * Math.cos(radAngle)), (float) (nRadius * Math.sin(radAngle)));
        Point2D nCollision = new Point2D(nCurPos.x() + nDelta.x(), nCurPos.y() + nDelta.y());
        float nDist = radius / MapConfig.BOX_SIZE;

        if (quadrant == 1 || quadrant == 2) {
            for (float y = (float) Math.ceil(nCurPos.y()); y <= Math.floor(nCurPos.y() + nDelta.y()); y++) {
                float x = (y - nCurPos.y()) / nDelta.y() * nDelta.x() + nCurPos.x();
                if (map.hasWall(WallType.UP_WALL, (int) x, (int) y - 1)) { // OK
                    nDist = updateDist(nDist, nCollision, nCurPos, x, y);
                    break;
                }
            }
        } else {
            for (float y = (float) Math.floor(nCurPos.y()); y >= Math.ceil(nCurPos.y() + nDelta.y()); y--) {
                float x = (y - nCurPos.y()) / nDelta.y() * nDelta.x() + nCurPos.x();
                if (map.hasWall(WallType.DOWN_WALL, (int) x, (int) y)) {
                    nDist = updateDist(nDist, nCollision, nCurPos, x, y);
                    break;
                }
            }
        }

        if (quadrant == 1 || quadrant == 4) {
            for (float x = (float) Math.ceil(nCurPos.x()); x <= Math.floor(nCurPos.x() + nDelta.x()); x++) {
                float y = (x - nCurPos.x()) / nDelta.x() * nDelta.y() + nCurPos.y();
                if (map.hasWall(WallType.RIGHT_WALL, (int) x - 1, (int) y)) { // OK
                    nDist = updateDist(nDist, nCollision, nCurPos, x, y);
                    break;
                }
            }
        } else {
            for (float x = (float) Math.floor(nCurPos.x()); x >= Math.ceil(nCurPos.x() + nDelta.x()); x--) {
                float y = (x - nCurPos.x()) / nDelta.x() * nDelta.y() + nCurPos.y();
                if (map.hasWall(WallType.LEFT_WALL, (int) x, (int) y)) {
                    nDist = updateDist(nDist, nCollision, nCurPos, x, y);
                    break;
                }
            }
        }
        return new Point2D(nCollision.x() * MapConfig.BOX_SIZE, nCollision.y() * MapConfig.BOX_SIZE);
    }
}
