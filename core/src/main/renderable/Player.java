package renderable;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import input.IPlayerInput;

public class Player implements Renderable{
    private final Sprite sprite;
    private final Vector2 position;
    private float rotation;
    private float speed;

    public Player(Vector2 position) {
        sprite = new Sprite(new Texture("player.png"));
        sprite.setSize(Tile.width, Tile.height);
        sprite.setOriginCenter();

        this.position = position;
        this.position.x *= Tile.width;
        this.position.y *= Tile.height;

        rotation = 0;
        speed = 3;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void updatePosition(IPlayerInput playerInput, float deltaTime) {
        position.x += playerInput.getX() * Tile.width * speed * deltaTime;
        position.y += playerInput.getY() * Tile.height * speed * deltaTime;
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
