package map.rendercontainers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import types.TextureType;

public class MapWall extends MapElement {

    public MapWall(int positionX, int positionY, int sizeX, int sizeY) {
        super(TextureRegion.split(TextureType.Wall.createTexture(), sizeX, sizeY)[0][0], positionX, positionY);
    }
}
