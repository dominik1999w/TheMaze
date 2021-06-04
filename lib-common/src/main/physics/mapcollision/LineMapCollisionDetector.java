package physics.mapcollision;

import java.util.ArrayList;
import java.util.List;

import map.Map;
import map.MapConfig;
import physics.HitboxHistory;
import types.WallType;
import util.Point2D;

import static util.MathUtils.floor;
import static util.MathUtils.ceil;

public class LineMapCollisionDetector extends MapCollisionDetector {

    public LineMapCollisionDetector(Map map) {
        super(map);
    }

    @Override
    public MapCollisionInfo detectMapCollision(HitboxHistory<?> history) {
        Point2D currentPosition = history.getPreviousPosition();
        Point2D targetPosition = new Point2D(history.getHitbox().getPosition());
        float distance = Point2D.dist(currentPosition, targetPosition);
        Point2D deltaPosition = targetPosition.subtract(currentPosition);

        float angle = (float)Math.atan2(deltaPosition.y(), deltaPosition.x());
        if (angle < 0) angle += 2 * Math.PI;

        return castRayAtMap(currentPosition, angle, distance);
    }

    /*
     * @angle in radians
     */
    private MapCollisionInfo castRayAtMap(Point2D rayStart, float angle, float rayLength) {
        int quadrant = (int) (2 * angle / Math.PI) + 1;

        rayStart = new Point2D(rayStart).divide(MapConfig.BOX_SIZE);
        rayLength /= MapConfig.BOX_SIZE;
        Point2D ray = new Point2D((float) (rayLength * Math.cos(angle)), (float) (rayLength * Math.sin(angle)));
        Point2D collisionPoint = new Point2D(rayStart).add(ray);
        float dist = rayLength;
        boolean hasCollided = false;
        Point2D cursor = new Point2D();

        float dx = (ray.y() == 0) ? 0 : ray.x() / ray.y();
        if (quadrant == 1 || quadrant == 2) {
            int y = ceil(rayStart.y());
            float x = (y - rayStart.y()) * dx + rayStart.x();
            for (int yLast = floor(rayStart.y() + ray.y()); y <= yLast; y++, x += dx) {
                cursor.set(x, y);
                if (map.hasWall(WallType.UP_WALL, (int) x, y - 1)) {
                    hasCollided = true;
                    dist = updateDist(dist, collisionPoint, rayStart, cursor);
                    break;
                }
            }
        } else {
            int y = floor(rayStart.y());
            float x = (y - rayStart.y()) * dx + rayStart.x();
            for (int yLast = ceil(rayStart.y() + ray.y()); y >= yLast; y--, x -= dx) {
                cursor.set(x, y);
                if (map.hasWall(WallType.DOWN_WALL, (int) x, y)) {
                    hasCollided = true;
                    dist = updateDist(dist, collisionPoint, rayStart, cursor);
                    break;
                }
            }
        }

        float dy = (ray.x() == 0) ? 0 : ray.y() / ray.x();
        if (quadrant == 1 || quadrant == 4) {
            int x = ceil(rayStart.x());
            float y = (x - rayStart.x()) * dy + rayStart.y();
            for (int xLast = floor(rayStart.x() + ray.x()); x <= xLast; x++, y += dy) {
                cursor.set(x, y);
                if (map.hasWall(WallType.RIGHT_WALL, x - 1, (int) y)) {
                    hasCollided = true;
                    dist = updateDist(dist, collisionPoint, rayStart, cursor);
                    break;
                }
            }
        } else {
            int x = floor(rayStart.x());
            float y = (x - rayStart.x()) * dy + rayStart.y();
            for (int xLast = ceil(rayStart.x() + ray.x()); x >= xLast; x--, y -= dy) {
                cursor.set(x, y);
                if (map.hasWall(WallType.LEFT_WALL, x, (int) y)) {
                    hasCollided = true;
                    dist = updateDist(dist, collisionPoint, rayStart, cursor);
                    break;
                }
            }
        }
        return new MapCollisionInfo(collisionPoint.multiply(MapConfig.BOX_SIZE), hasCollided);
    }

    public List<float[]> getSightTriangles(Point2D playerPosition, float viewRadius, int numberOfRays) {
        List<Point2D> maxRange = new ArrayList<>();
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

    private float updateDist(float oldDist, Point2D collisionPoint, Point2D rayStart, Point2D cursor) {
        float newDist = Point2D.dist(rayStart, cursor);
        if (newDist < oldDist) {
            collisionPoint.set(cursor);
            return newDist;
        }
        return oldDist;
    }

    private Point2D detectLightingMapCollisions(Point2D currentPosition, float angle/*deg*/, float radius) {
        angle -= (int) (angle / 360) * 360;
        return castRayAtMap(currentPosition, (float)Math.toRadians(angle), radius).nextPosition;
    }
}
