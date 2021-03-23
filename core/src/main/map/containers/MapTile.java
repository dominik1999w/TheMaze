package map.containers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.List;

import map.config.MapConfig;
import types.TextureType;
import types.WallTypes;

public class MapTile extends MapElement {

    private final List<MapWall> walls;

    public MapTile(int positionX, int positionY, List<WallTypes> walls) {
        this.textureRegion = TextureRegion.split(TextureType.Ground.createTexture(), MapConfig.BOX_SIZE, MapConfig.BOX_SIZE)[0][0];
        this.positionX = positionX;
        this.positionY = positionY;

        this.walls = new ArrayList<>();
        for (WallTypes type : walls) {
            this.walls.add(type.createWall(MapConfig.BOX_SIZE, positionX, positionY, MapConfig.WALL_THICKNESS));
        }
    }

    public List<MapWall> getWalls() {
        return walls;
    }
}
