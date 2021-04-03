package db;

import java.util.ArrayList;
import java.util.List;

import map.Map;
import map.rendercontainers.MapTile;
import renderable.MapView;

public class Database implements DatabaseGateway {
    private final DatabaseEngine databaseEngine;

    public Database(DatabaseEngine databaseEngine) {
        this.databaseEngine = databaseEngine;
    }

    @Override
    public MapView loadMap() {
        List<MapTile> tiles = new ArrayList<>();
        databaseEngine.readMap((x, y, walls) -> tiles.add(new MapTile(x, y, walls)));
        return new MapView(tiles);
    }

    @Override
    public void saveMap(Map map) {
        databaseEngine.saveMap(map);
    }


}
