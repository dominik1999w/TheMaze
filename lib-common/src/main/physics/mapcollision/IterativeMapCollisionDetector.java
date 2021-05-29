package physics.mapcollision;

import map.Map;
import map.MapConfig;
import physics.HitboxHistory;
import types.WallType;
import util.Point2D;

public class IterativeMapCollisionDetector extends MapCollisionDetector {
    private final int FREQUENCY = 10;

    public IterativeMapCollisionDetector(Map map) {
        super(map);
    }

    @Override
    public MapCollisionInfo detectMapCollision(HitboxHistory<?> history) {
        Point2D delta_position = new Point2D(history.getHitbox().getPosition())
                .subtract(history.getPreviousPosition());
        Point2D position = new Point2D(history.getPreviousPosition());
        Point2D projected_pos = new Point2D(history.getPreviousPosition());
        Point2D delta_position_fragment = new Point2D(delta_position).divide(FREQUENCY);
        float hitboxRadius = history.getHitbox().getRadius();

        boolean hasCollided = false;
        for(int i = 0; i < FREQUENCY; i++) {
            projected_pos.add(delta_position_fragment);

            if (!verticalWallCollision(projected_pos, hitboxRadius) && !horizontalWallCollision(projected_pos, hitboxRadius)) {
                hasCollided = false;
                position = new Point2D(projected_pos);
            } else {
                hasCollided = true;
                boolean only_x = !verticalWallCollision(
                        new Point2D(projected_pos.x(), position.y()), hitboxRadius
                ) && !horizontalWallCollision(
                        new Point2D(projected_pos.x(), position.y()), hitboxRadius
                );
                boolean only_y = !verticalWallCollision(
                        new Point2D(position.x(), projected_pos.y()), hitboxRadius
                ) && !horizontalWallCollision(
                        new Point2D(position.x(), projected_pos.y()), hitboxRadius
                );

                if (Math.abs(delta_position.x()) >= Math.abs(delta_position.y())) {
                    if (only_x) {
                        position.set(projected_pos.x(), position.y());
                    } else if (only_y) {
                        position.set(position.x(), projected_pos.y());
                    } else {
                        break;
                    }
                } else {
                    if (only_y) {
                        position.set(position.x(), projected_pos.y());
                    } else if (only_x) {
                        position.set(projected_pos.x(), position.y());
                    } else {
                        break;
                    }
                }
            }
        }

        return new MapCollisionInfo(position, hasCollided);
    }

    private boolean verticalWallCollision(Point2D position, float hitboxRadius) {
        Point2D pos = new Point2D(position).divide(MapConfig.BOX_SIZE);
        int vl_x = Math.round(pos.x());
        float vl_half2 = hitboxRadius * hitboxRadius - (vl_x - pos.x()) * (vl_x - pos.x());
        if (vl_half2 < 0) {
            return false;
        }
        float vl_half = (float) Math.sqrt(vl_half2);
        int vl_y_min = Math.round(pos.y() - vl_half - 0.5f);
        int vl_y_max = Math.round(pos.y() + vl_half - 0.5f);
        return map.hasWall(WallType.LEFT_WALL, vl_x, vl_y_min) || map.hasWall(WallType.LEFT_WALL, vl_x, vl_y_max);
    }

    private boolean horizontalWallCollision(Point2D position, float hitboxRadius) {
        Point2D pos = new Point2D(position).divide(MapConfig.BOX_SIZE);
        int hl_y = Math.round(pos.y());
        float hl_half2 = hitboxRadius * hitboxRadius - (hl_y - pos.y()) * (hl_y - pos.y());
        if (hl_half2 < 0) {
            return false;
        }
        float hl_half = (float) Math.sqrt(hl_half2);
        int hl_x_min = Math.round(pos.x() - hl_half - 0.5f);
        int hl_x_max = Math.round(pos.x() + hl_half - 0.5f);
        return map.hasWall(WallType.DOWN_WALL, hl_x_min, hl_y) || map.hasWall(WallType.DOWN_WALL, hl_x_max, hl_y);
    }
}
