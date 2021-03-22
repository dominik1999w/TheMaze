package loader;

import db.Database;
import renderable.Map;

public class GameLoader {

    private final Database database;

    public GameLoader(Database database) {
        this.database = database;
    }

    public Map getTileMap() {
        return database.loadMap();
    }
}
