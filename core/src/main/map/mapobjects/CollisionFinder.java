package map.mapobjects;

import map.Map;
import map.config.MapConfig;
import types.WallType;
import util.Point2D;

public class CollisionFinder {
    private final int FREQUENCY = 10;

    private final Map map;
    private final float hitboxRadius;
    private boolean foundCollision;

    public CollisionFinder(Map map, float hitboxRadius) {
        this.map = map;
        this.hitboxRadius = hitboxRadius;
        this.foundCollision = false;
    }

    private boolean verticalWallCollision(Point2D position) {
        Point2D pos = new Point2D(position.x / MapConfig.BOX_SIZE, position.y / MapConfig.BOX_SIZE);
        int vl_x = Math.round(pos.x + 0.5f);
        float vl_half2 = hitboxRadius * hitboxRadius - (vl_x - 0.5f - pos.x) * (vl_x - 0.5f - pos.x);
        if (vl_half2 < 0) {
            return false;
        }
        float vl_half = (float) Math.sqrt(vl_half2);
        int vl_y_min = Math.round(pos.y - vl_half);
        int vl_y_max = Math.round(pos.y + vl_half);
        return map.hasWall(WallType.LEFT_WALL, vl_x, vl_y_min) || map.hasWall(WallType.LEFT_WALL, vl_x, vl_y_max);
    }

    private boolean horizontalWallCollision(Point2D position) {
        Point2D pos = new Point2D(position.x / MapConfig.BOX_SIZE, position.y / MapConfig.BOX_SIZE);
        int hl_y = Math.round(pos.y + 0.5f);
        float hl_half2 = hitboxRadius * hitboxRadius - (hl_y - 0.5f - pos.y) * (hl_y - 0.5f - pos.y);
        if (hl_half2 < 0) {
            return false;
        }
        float hl_half = (float) Math.sqrt(hl_half2);
        int hl_x_min = Math.round(pos.x - hl_half);
        int hl_x_max = Math.round(pos.x + hl_half);
        return map.hasWall(WallType.DOWN_WALL, hl_x_min, hl_y) || map.hasWall(WallType.DOWN_WALL, hl_x_max, hl_y);
    }

    public Point2D getNewPosition(Point2D initial_position, Point2D delta_position) {
        Point2D position = new Point2D(initial_position);
        Point2D projected_pos = new Point2D(initial_position);

        for(int i = 0; i < FREQUENCY; i++) {
            projected_pos.x += delta_position.x / FREQUENCY;
            projected_pos.y += delta_position.y / FREQUENCY;

            if (!verticalWallCollision(projected_pos) && !horizontalWallCollision(projected_pos)) {
                foundCollision = false;
                position.x = projected_pos.x;
                position.y = projected_pos.y;
            } else {
                foundCollision = true;
                boolean only_x = !verticalWallCollision(new Point2D(projected_pos.x, position.y)) && !horizontalWallCollision(new Point2D(projected_pos.x, position.y));
                boolean only_y = !verticalWallCollision(new Point2D(position.x, projected_pos.y)) && !horizontalWallCollision(new Point2D(position.x, projected_pos.y));

                if (Math.abs(delta_position.x) >= Math.abs(delta_position.y)) {
                    if (only_x) {
                        position.x = projected_pos.x;
                    } else if (only_y) {
                        position.y = projected_pos.y;
                    } else {
                        break;
                    }
                } else {
                    if (only_y) {
                        position.y = projected_pos.y;
                    } else if (only_x) {
                        position.x = projected_pos.x;
                    } else {
                        break;
                    }
                }
            }
        }
        return position;
    }

    public boolean found() {
        return foundCollision;
    }
}
