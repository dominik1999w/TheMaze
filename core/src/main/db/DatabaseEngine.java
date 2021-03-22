package db;

import java.util.List;

import map.generator.Node;
import types.WallTypes;

public interface DatabaseEngine {

    interface CellListBuilder {
        void buildCell(int x, int y, List<WallTypes> boarders);
    }

    void readMap(CellListBuilder builder);

    void saveMap(Node[][] map);
}
