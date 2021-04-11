package renderable;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import map.config.MapConfig;
import map.mapobjects.Bullet;
import util.Point2D;

public class BulletView implements Renderable {
    private final Bullet bullet;
    private final Sprite sprite;

    public BulletView(Bullet bullet) {
        this.bullet = bullet;
        sprite = new Sprite(new Texture("bullet.png"));
        sprite.setSize(MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
        sprite.setOriginCenter();
    }

    public void updatePosition() {
        Point2D position = bullet.getPosition();
        sprite.setPosition(position.x(), position.y());
        sprite.setRotation(bullet.getRotation());
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        sprite.draw(spriteBatch);
    }
}
