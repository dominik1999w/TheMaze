package renderable;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import input.IPlayerInput;

public class Player implements Renderable{
    private final Texture texture;
    private final Vector2 position;
    private float speed;

    public Player(Vector2 position) {
        this.texture = new Texture("player.png");
        this.position = position;
        this.position.x *= Tile.width;
        this.position.y *= Tile.height;
        speed = 3;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void updatePosition(IPlayerInput playerInput, float deltaTime) {
        position.x += playerInput.getX() * Tile.width * speed * deltaTime;
        position.y += playerInput.getY() * Tile.height * speed * deltaTime;
    }

    public void shoot() {
        System.out.println("SHOOT!");
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        spriteBatch.draw(texture, position.x, position.y, Tile.width, Tile.height);
    }
}
