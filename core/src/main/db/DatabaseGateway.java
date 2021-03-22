package db;

import map.generator.MapGenerator;
import renderable.Map;

public interface DatabaseGateway {
    Map loadMap();

    void saveMap(MapGenerator.Node[][] map);
}
