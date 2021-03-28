package db;

import java.util.List;

import map.generator.MapGenerator;
import types.WallType;

public interface DatabaseEngine {

    interface CellListBuilder {
        void buildCell(int x, int y, List<WallType> walls);
    }

    void readMap(CellListBuilder builder);

    void saveMap(MapGenerator.Node[][] map);
}
