package renderable;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import map.config.MapConfig;
import player.Player;
import renderable.Renderable;
import util.Point2D;

public class RemotePlayerView implements Renderable {

    private final Player player;
    private final Sprite sprite;

    public RemotePlayerView(Player player, Texture spriteTexture) {
        this.player = player;
        this.sprite = new Sprite(spriteTexture);
        sprite.setSize(MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
        sprite.setOriginCenter();
    }

    @Override
    public void render(SpriteBatch batch) {
        Point2D position = player.getPosition();
        sprite.setPosition(position.x(), position.y());
        sprite.setRotation(player.getRotation());
        sprite.draw(batch);
    }
}
