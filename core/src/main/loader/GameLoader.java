package loader;

import db.Database;
import renderable.MapView;

public class GameLoader {

    private final Database database;

    public GameLoader(Database database) {
        this.database = database;
    }

    public MapView getTileMap() {
        return database.loadMap();
    }
}
