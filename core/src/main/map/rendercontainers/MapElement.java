package map.rendercontainers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MapElement {
    protected TextureRegion textureRegion;
    protected int positionX;
    protected int positionY;

    protected MapElement(TextureRegion textureRegion, int positionX, int positionY) {
        this.textureRegion = textureRegion;
        this.positionX = positionX;
        this.positionY = positionY;
    }

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
