package loader;

import db.Database;
import renderable.TileMap;

public class GameLoader {

    private final Database database;

    public GameLoader(Database database) {
        this.database = database;
    }

    public TileMap getTileMap() {
        return database.loadMap();
    }
}
