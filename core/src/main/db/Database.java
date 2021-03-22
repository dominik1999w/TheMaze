package db;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import map.generator.Node;
import map.containers.MapTile;
import renderable.Map;

public class Database implements DatabaseGateway {
    private final DatabaseEngine databaseEngine;

    public Database(DatabaseEngine databaseEngine) {
        this.databaseEngine = databaseEngine;
    }

    @Override
    public Map loadMap() {
        List<MapTile> tiles = new ArrayList<>();
        databaseEngine.readMap((x, y, boarders) -> tiles.add(new MapTile(new Vector2(x, y), boarders)));
        return new Map(tiles);
    }

    @Override
    public void saveMap(Node[][] map) {
        databaseEngine.saveMap(map);
    }


}
