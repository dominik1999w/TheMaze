package map.containers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MapElement {
    protected TextureRegion textureRegion;
    protected int positionX;
    protected int positionY;

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }
}
