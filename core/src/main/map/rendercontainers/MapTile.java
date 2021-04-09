package map.rendercontainers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.List;

import map.config.MapConfig;
import types.TextureType;
import types.WallType;
import util.Point2D;
import util.Shape2D;

public class MapTile extends MapElement {

    private final List<MapWall> walls;

    public MapTile(Shape2D shape, List<WallType> walls) {
        super(TextureRegion.split(TextureType.Ground.createTexture(), MapConfig.BOX_SIZE, MapConfig.BOX_SIZE)[0][0], shape);

        this.walls = new ArrayList<>();
        for (WallType type : walls) {
            int length = MapConfig.BOX_SIZE;
            Shape2D wallShape = new Shape2D(shape.getPosition(), new Point2D(length, MapConfig.WALL_THICKNESS));
            this.walls.add(new MapWall(type.getWallShape(wallShape)));
        }
    }

    public List<MapWall> getWalls() {
        return walls;
    }
}
