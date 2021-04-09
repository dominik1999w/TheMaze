package map.rendercontainers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import util.Shape2D;

public class MapElement {
    protected final TextureRegion textureRegion;
    protected final Shape2D shape;

    protected MapElement(TextureRegion textureRegion, Shape2D shape) {
        this.textureRegion = textureRegion;
        this.shape = shape;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public Shape2D getShape() {
        return shape;
    }
}
