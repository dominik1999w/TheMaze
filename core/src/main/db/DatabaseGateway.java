package db;

import map.Map;
import renderable.MapView;

public interface DatabaseGateway {
    MapView loadMap();

    void saveMap(Map map);
}
