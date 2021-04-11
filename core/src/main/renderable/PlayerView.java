package renderable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import input.IPlayerInput;
import map.config.MapConfig;
import map.mapobjects.Bullet;
import map.mapobjects.Player;
import types.TextureType;
import util.Point2D;

public class PlayerView implements Renderable {
    private final Player player;
    private final Sprite sprite;
    private BulletView bulletView;
    private final AssetManager assetManager;

    public PlayerView(Player player, Texture spriteTexture, AssetManager assetManager) {
        this.player = player;
        sprite = new Sprite(spriteTexture);
        this.assetManager = assetManager;
        sprite.setSize(MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
        sprite.setOriginCenter();
    }

    public void updateFromInput(IPlayerInput playerInput, float delta) {
        if(playerInput.isShootPressed()) player.shoot();
        player.updatePosition(playerInput, delta);
        updatePosition();
    }

    public void updatePosition() {
        Point2D position = player.getPosition();
        sprite.setPosition(position.x(), position.y());
        sprite.setRotation(player.getRotation());
        Bullet bullet = player.getBullet();
        if(player.getBullet() != null) {
            if(bulletView == null) {
                bulletView = new BulletView(bullet, assetManager.get(TextureType.BULLET.getName()));
            }
        } else {
            bulletView = null;
        }
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        sprite.draw(spriteBatch);
        if(bulletView != null) {
            bulletView.render(spriteBatch);
        }
    }
}
