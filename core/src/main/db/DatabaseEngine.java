package db;

public interface DatabaseEngine {

    interface CellListBuilder {
        void buildCell(int type, int x, int y);
    }

    void readMap(CellListBuilder builder);
}
