package map.containers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import types.TextureType;

public class MapWall {
    private final TextureRegion textureRegion;
    private final Vector2 position;

    public MapWall(Vector2 position, Vector2 sizes) {
        this.textureRegion = TextureRegion.split(TextureType.Wall.createTexture(), (int) sizes.x, (int) sizes.y)[0][0];
        this.position = position;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public Vector2 getPosition() {
        return position;
    }
}
