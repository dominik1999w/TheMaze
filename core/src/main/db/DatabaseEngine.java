package db;

import java.util.List;

import map.generator.MapGenerator;
import types.WallTypes;

public interface DatabaseEngine {

    interface CellListBuilder {
        void buildCell(int x, int y, List<WallTypes> walls);
    }

    void readMap(CellListBuilder builder);

    void saveMap(MapGenerator.Node[][] map);
}
