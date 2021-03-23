package renderable;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import input.IPlayerInput;
import map.config.MapConfig;
import map.generator.MapGenerator;
import types.WallTypes;

public class Player implements Renderable{
    private final Sprite sprite;
    private final Vector2 position;
    private float rotation;
    private float speed;
    private final float hitbox_radius = 0.45f; // normalized to 1x1 tile size
    private MapGenerator mapGenerator;
    private int physics_speed = 3;

    public Player(Vector2 position, MapGenerator mapGenerator) {
        sprite = new Sprite(new Texture("player.png"));
        sprite.setSize(MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
        sprite.setOriginCenter();

        this.position = position;
        this.position.x *= MapConfig.BOX_SIZE;
        this.position.y *= MapConfig.BOX_SIZE;
        this.mapGenerator = mapGenerator;

        rotation = 0;
        speed = 3;
    }

    public Vector2 getPosition() {
        return position;
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
        return mapGenerator.hasWall(WallTypes.LEFT_WALL, vl_x, vl_y_min) || mapGenerator.hasWall(WallTypes.LEFT_WALL, vl_x, vl_y_max);
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
        return mapGenerator.hasWall(WallTypes.DOWN_WALL, hl_x_min, hl_y) || mapGenerator.hasWall(WallTypes.DOWN_WALL, hl_x_max, hl_y);
    }

    public void updatePosition(IPlayerInput playerInput, float deltaTime) {
        Vector2 projected_pos = new Vector2(position);

        for(int i = 0; i < physics_speed; i++) {
            projected_pos.x += playerInput.getX() * MapConfig.BOX_SIZE * speed * deltaTime / (float) physics_speed;
            projected_pos.y += playerInput.getY() * MapConfig.BOX_SIZE * speed * deltaTime / (float) physics_speed;

            if (!verticalWallCollision(projected_pos) && !horizontalWallCollision(projected_pos)) {
                position.x = projected_pos.x;
                position.y = projected_pos.y;
            } else {
                boolean only_x = !verticalWallCollision(new Vector2(projected_pos.x, position.y)) && !horizontalWallCollision(new Vector2(projected_pos.x, position.y));
                boolean only_y = !verticalWallCollision(new Vector2(position.x, projected_pos.y)) && !horizontalWallCollision(new Vector2(position.x, projected_pos.y));

                if (Math.abs(playerInput.getX()) >= Math.abs(playerInput.getY())) {
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

        if (playerInput.getX() != 0 || playerInput.getY() != 0) {
            rotation = (float) (Math.atan2(playerInput.getY(), playerInput.getX()) * (180 / Math.PI));
        }

        sprite.setPosition(position.x, position.y);
        sprite.setRotation(rotation);
    }

    public void shoot() {
        System.out.println("SHOOT!");
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        sprite.draw(spriteBatch);
    }
}
