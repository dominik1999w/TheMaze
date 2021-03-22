package map.containers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import map.config.MapConfig;
import types.TextureType;
import types.WallTypes;

public class MapTile {

    private final TextureRegion textureRegion;

    private final Vector2 position;

    private final List<MapWall> walls;

    public MapTile(Vector2 position, List<WallTypes> boarders) {
        this.textureRegion = TextureRegion.split(TextureType.Ground.createTexture(), MapConfig.BOX_SIZE, MapConfig.BOX_SIZE)[0][0];
        this.position = position;
        this.walls = new ArrayList<>();
        for (WallTypes type : boarders) {
            walls.add(type.createWall(MapConfig.BOX_SIZE, position.x, position.y, MapConfig.WALL_THICKNESS));
        }
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public Vector2 getPosition() {
        return position;
    }

    public List<MapWall> getWalls() {
        return walls;
    }
}
