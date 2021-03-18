package renderable;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import types.TextureType;

public class Tile implements Renderable {
    public static final int height = 32;
    public static final int width = 32;

    private final TextureRegion textureRegion;
    private final Vector2 position;

    public Tile(TextureType texture, Vector2 position) {
        this.textureRegion = TextureRegion.split(texture.createTexture(), width, height)[0][0];
        this.position = position;
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        spriteBatch.draw(textureRegion, position.x, position.y);
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public Vector2 getPosition() {
        return position;
    }
}
