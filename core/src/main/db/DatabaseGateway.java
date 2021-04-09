package db;

import map.Map;

public interface DatabaseGateway {
    Map loadMap();

    void saveMap(Map map);
}
