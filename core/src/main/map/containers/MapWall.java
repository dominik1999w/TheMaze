package map.containers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import types.TextureType;

public class MapWall extends MapElement {

    public MapWall(Vector2 position, Vector2 sizes) {
        this.textureRegion = TextureRegion.split(TextureType.Wall.createTexture(), (int) sizes.x, (int) sizes.y)[0][0];
        this.position = position;
    }
}
