//package db;
//
//import map.Map;
//import map.MapConfig;
//
//public class Database implements DatabaseGateway {
//    private final DatabaseEngine databaseEngine;
//
//    public Database(DatabaseEngine databaseEngine) {
//        this.databaseEngine = databaseEngine;
//    }
//
//    @Override
//    public Map loadMap() {
//        Map.Node[][] map = new Map.Node[MapConfig.MAP_LENGTH][MapConfig.MAP_LENGTH];
//        databaseEngine.readMap((x, y, walls) -> map[x][y] = new Map.Node(x, y, walls));
//        return new Map(map);
//    }
//
//    @Override
//    public void saveMap(Map map) {
//        databaseEngine.saveMap(map);
//    }
//
//
//}
