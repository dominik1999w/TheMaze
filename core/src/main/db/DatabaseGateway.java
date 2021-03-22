package db;

import map.generator.Node;
import renderable.Map;

public interface DatabaseGateway {
    Map loadMap();

    void saveMap(Node[][] map);
}
