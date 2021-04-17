package renderable;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.UUID;

import map.MapConfig;
import util.Point2D;
import entity.WorldEntity;

public class SimpleView<T extends WorldEntity> implements Renderable {

    private final T object;
    private final Sprite sprite;

    public SimpleView(T object, Texture spriteTexture) {
        this.object = object;
        sprite = new Sprite(spriteTexture);
        sprite.setSize(MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
        sprite.setOriginCenter();
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        Point2D position = object.getPosition();
        sprite.setOriginBasedPosition(position.x(), position.y());
        sprite.setRotation(object.getRotation());
        sprite.draw(spriteBatch);
    }

    public UUID getId() {
        return object.getId();
    }
}
