package map.generator;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import map.Map;
import map.config.MapConfig;
import types.WallType;

import static types.WallType.DOWN_WALL;
import static types.WallType.LEFT_WALL;
import static types.WallType.RIGHT_WALL;
import static types.WallType.UP_WALL;

public class MapGenerator {
    private final Map.Node[][] graph;
    private final float walls_to_remove = 0.5f;


    public MapGenerator() {
        graph = new Map.Node[MapConfig.MAP_LENGTH][MapConfig.MAP_LENGTH];
    }

    public Map.Node[][] generateMap() {
        for (int i = 0; i < MapConfig.MAP_LENGTH; i++) {
            for (int j = 0; j < MapConfig.MAP_LENGTH; j++) {
                graph[i][j] = new Map.Node();
                graph[i][j].addWall(UP_WALL);
                graph[i][j].addWall(DOWN_WALL);
                graph[i][j].addWall(LEFT_WALL);
                graph[i][j].addWall(RIGHT_WALL);
            }
        }
        boolean[][] visited = new boolean[MapConfig.MAP_LENGTH][MapConfig.MAP_LENGTH];
        dfs(graph, visited, 0, 0);

        for (int i = 1; i < MapConfig.MAP_LENGTH; i++) {
            for (int j = 1; j < MapConfig.MAP_LENGTH; j++) {
                if (Math.random() < walls_to_remove) {
                    graph[i][j].removeWall(DOWN_WALL);
                    graph[i][j - 1].removeWall(UP_WALL);
                }
                if (Math.random() < walls_to_remove) {
                    graph[i][j].removeWall(LEFT_WALL);
                    graph[i - 1][j].removeWall(RIGHT_WALL);
                }
            }
        }

        return graph;
    }

    private void dfs(Map.Node[][] graph, boolean[][] visited, int i, int j) {
        visited[i][j] = true;
        List<WallType> wallList = Arrays.asList(WallType.values());
        Collections.shuffle(wallList);

        for (WallType wall : wallList) {
            if (!graph[i][j].hasWall(wall)) {
                continue;
            }
            int newI = i + wall.getRelativePositionX();
            int newJ = j + wall.getRelativePositionY();
            if (newI < 0 || newI >= MapConfig.MAP_LENGTH || newJ < 0 || newJ >= MapConfig.MAP_LENGTH || visited[newI][newJ]) {
                continue;
            }
            graph[i][j].removeWall(wall);
            graph[newI][newJ].removeWall(wall.getOppositeWall());
            dfs(graph, visited, newI, newJ);
        }
    }
}


