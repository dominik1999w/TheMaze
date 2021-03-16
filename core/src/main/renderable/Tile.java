package renderable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import types.TextureType;

public class Tile implements RenderEngine {
    private final Texture texture;
    private final Vector2 position;

    public Tile(TextureType texture, Vector2 position) {
        this.texture = texture.createTexture();
        this.position = position;
        /* TODO: fix resolution issues => generate map which is suitable for universal devices */
        /* size of a map should correspond to width/height ratio, but should remain the same for all
            devices.
         */
        float ratio = (float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
        this.position.x = (float) Gdx.graphics.getWidth() / 50f * position.x;
        this.position.y = (float) Gdx.graphics.getHeight() / 50f * ratio * position.y;
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        spriteBatch.draw(texture, position.x, position.y);
    }
}
