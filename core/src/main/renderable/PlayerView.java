package renderable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import map.config.MapConfig;
import map.mapobjects.OPlayer;
import types.TextureType;
import util.Point2D;

public class PlayerView implements Renderable {
    private final OPlayer player;
    private final Sprite sprite;
    private BulletView bulletView;
    private final AssetManager assetManager;

    public PlayerView(OPlayer player, Texture spriteTexture, AssetManager assetManager) {
        this.player = player;
        sprite = new Sprite(spriteTexture);
        this.assetManager = assetManager;
        sprite.setSize(MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
        sprite.setOriginCenter();
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        Point2D position = player.getPosition();
        sprite.setPosition(position.x(), position.y());
        sprite.setRotation(player.getRotation());
        sprite.draw(spriteBatch);
        if (player.getBullet() != null) {
            if (bulletView == null) {
                bulletView = new BulletView(player.getBullet(), assetManager.get(TextureType.BULLET.getName()));
            }
            bulletView.render(spriteBatch);
        } else {
            bulletView = null;
        }
    }
}
