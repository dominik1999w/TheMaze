package db;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import renderable.Tile;
import renderable.TileMap;
import types.TextureType;

public class Database implements DatabaseGateway {
    private final DatabaseEngine databaseEngine;

    public Database(DatabaseEngine databaseEngine) {
        this.databaseEngine = databaseEngine;
    }

    @Override
    public TileMap loadMap() {
        List<Tile> tiles = new ArrayList<>();
        databaseEngine.readMap((type, x, y) -> tiles.add(new Tile(TextureType.values()[type], new Vector2(x, y))));
        return new TileMap(tiles);
    }
}
