package renderable;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import input.IPlayerInput;
import map.config.MapConfig;

public class Player implements Renderable{
    private final Sprite sprite;
    private final Vector2 position;
    private float rotation;
    private float speed;

    public Player(Vector2 position) {
        sprite = new Sprite(new Texture("player.png"));
        sprite.setSize(MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
        sprite.setOriginCenter();

        this.position = position;
        this.position.x *= MapConfig.BOX_SIZE;
        this.position.y *= MapConfig.BOX_SIZE;

        rotation = 0;
        speed = 3;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void updatePosition(IPlayerInput playerInput, float deltaTime) {
        position.x += playerInput.getX() * MapConfig.BOX_SIZE * speed * deltaTime;
        position.y += playerInput.getY() *MapConfig.BOX_SIZE * speed * deltaTime;
        if(playerInput.getX() != 0 || playerInput.getY() != 0) {
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
