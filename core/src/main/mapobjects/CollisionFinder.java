package mapobjects;

import com.badlogic.gdx.math.Vector2;

import map.config.MapConfig;
import map.generator.MapGenerator;
import types.WallType;

public class CollisionFinder {
    private static final int FREQUENCY = 3;

    private MapGenerator mapGenerator;
    private float hitbox_radius;

    public CollisionFinder(MapGenerator mapGenerator, float hitbox_radius) {
        this.mapGenerator = mapGenerator;
        this.hitbox_radius = hitbox_radius;
    }

    private boolean verticalWallCollision(Vector2 position) {
        Vector2 pos = new Vector2(position.x / MapConfig.BOX_SIZE, position.y / MapConfig.BOX_SIZE);
        int vl_x = (int)Math.round(pos.x + 0.5f);
        float vl_half2 = hitbox_radius * hitbox_radius - (vl_x - 0.5f - pos.x) * (vl_x - 0.5f - pos.x);
        if (vl_half2 < 0) {
            return false;
        }
        float vl_half = (float) Math.sqrt(vl_half2);
        int vl_y_min = (int)Math.round(pos.y - vl_half);
        int vl_y_max = (int)Math.round(pos.y + vl_half);
        return mapGenerator.hasWall(WallType.LEFT_WALL, vl_x, vl_y_min) || mapGenerator.hasWall(WallType.LEFT_WALL, vl_x, vl_y_max);
    }

    private boolean horizontalWallCollision(Vector2 position) {
        Vector2 pos = new Vector2(position.x / MapConfig.BOX_SIZE, position.y / MapConfig.BOX_SIZE);
        int hl_y = (int)Math.round(pos.y + 0.5f);
        float hl_half2 = hitbox_radius * hitbox_radius - (hl_y - 0.5f - pos.y) * (hl_y - 0.5f - pos.y);
        if (hl_half2 < 0) {
            return false;
        }
        float hl_half = (float) Math.sqrt(hl_half2);
        int hl_x_min = (int)Math.round(pos.x - hl_half);
        int hl_x_max = (int)Math.round(pos.x + hl_half);
        return mapGenerator.hasWall(WallType.DOWN_WALL, hl_x_min, hl_y) || mapGenerator.hasWall(WallType.DOWN_WALL, hl_x_max, hl_y);
    }

    public Vector2 getNewPosition(Vector2 initial_position, Vector2 delta_position) {
        Vector2 position = new Vector2(initial_position);
        Vector2 projected_pos = new Vector2(initial_position);

        for(int i = 0; i < FREQUENCY; i++) {
            projected_pos.x += delta_position.x / FREQUENCY;
            projected_pos.y += delta_position.y / FREQUENCY;

            if (!verticalWallCollision(projected_pos) && !horizontalWallCollision(projected_pos)) {
                position.x = projected_pos.x;
                position.y = projected_pos.y;
            } else {
                boolean only_x = !verticalWallCollision(new Vector2(projected_pos.x, position.y)) && !horizontalWallCollision(new Vector2(projected_pos.x, position.y));
                boolean only_y = !verticalWallCollision(new Vector2(position.x, projected_pos.y)) && !horizontalWallCollision(new Vector2(position.x, projected_pos.y));

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
}
