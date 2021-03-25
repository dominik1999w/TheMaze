package renderable;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import map.config.MapConfig;

public class PlayerView implements Renderable {
    private final Sprite sprite;

    public PlayerView() {
        sprite = new Sprite(new Texture("player.png"));
        sprite.setSize(MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
        sprite.setOriginCenter();
    }

    public void setPosition(Vector2 position) {
        sprite.setPosition(position.x, position.y);
    }

    public void setRotation(float angle) {
        sprite.setRotation(angle);
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        sprite.draw(spriteBatch);
    }
}
