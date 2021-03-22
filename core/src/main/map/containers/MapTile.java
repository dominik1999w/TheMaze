package map.containers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import map.config.MapConfig;
import types.TextureType;
import types.WallTypes;

public class MapTile extends MapElement {

    private final List<MapWall> walls;

    public MapTile(Vector2 position, List<WallTypes> walls) {
        this.textureRegion = TextureRegion.split(TextureType.Ground.createTexture(), MapConfig.BOX_SIZE, MapConfig.BOX_SIZE)[0][0];
        this.position = position;

        this.walls = new ArrayList<>();
        for (WallTypes type : walls) {
            this.walls.add(type.createWall(MapConfig.BOX_SIZE, position.x, position.y, MapConfig.WALL_THICKNESS));
        }
    }

    public List<MapWall> getWalls() {
        return walls;
    }
}