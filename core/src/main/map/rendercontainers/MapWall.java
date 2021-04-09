package map.rendercontainers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import types.TextureType;
import util.Shape2D;

public class MapWall extends MapElement {

    public MapWall(Shape2D shape) {
        super(TextureRegion.split(TextureType.Wall.createTexture(), (int) shape.getSize().x, (int) shape.getSize().y)[0][0], shape);
    }
}
