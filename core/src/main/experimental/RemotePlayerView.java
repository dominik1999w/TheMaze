package experimental;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import map.config.MapConfig;
import player.RemotePlayer;
import renderable.Renderable;

public class RemotePlayerView implements Renderable {

    private final RemotePlayer player;

    private final Sprite sprite;

    public RemotePlayerView(RemotePlayer player, Texture spriteTexture) {
        this.player = player;
        this.sprite = new Sprite(spriteTexture);
        sprite.setSize(MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
        sprite.setOriginCenter();
    }

    @Override
    public void render(SpriteBatch batch) {
        sprite.setPosition(player.getX(), player.getY());
        sprite.draw(batch);
    }
}
