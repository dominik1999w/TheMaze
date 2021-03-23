package map.generator;


import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import map.config.MapConfig;
import types.WallTypes;

import static types.WallTypes.DOWN_WALL;
import static types.WallTypes.LEFT_WALL;
import static types.WallTypes.RIGHT_WALL;
import static types.WallTypes.UP_WALL;

public class MapGenerator {
    private final Node[][] graph;

    public MapGenerator() {
        graph = new Node[MapConfig.MAP_LENGTH][MapConfig.MAP_LENGTH];
    }

    public Node[][] generateMap() {
        for (int i = 0; i < MapConfig.MAP_LENGTH; i++) {
            for (int j = 0; j < MapConfig.MAP_LENGTH; j++) {
                graph[i][j] = new Node(i, j);
                graph[i][j].addWall(UP_WALL);
                graph[i][j].addWall(DOWN_WALL);
                graph[i][j].addWall(LEFT_WALL);
                graph[i][j].addWall(RIGHT_WALL);
            }
        }
        boolean[][] visited = new boolean[MapConfig.MAP_LENGTH][MapConfig.MAP_LENGTH];
        for (int i = 0; i < MapConfig.MAP_LENGTH; i++) {
            for (int j = 0; j < MapConfig.MAP_LENGTH; j++) {
                if (!visited[i][j]) {
                    System.out.println(i + " " + j);
                    dfs(graph, visited, i, j);
                    return graph;
                }
            }
        }

        return graph;
    }

    private void dfs(Node[][] graph, boolean[][] visited, int i, int j) {
        visited[i][j] = true;
        List<WallTypes> wallList = Arrays.asList(WallTypes.values());
        Collections.shuffle(wallList);

        for (WallTypes wall : wallList) {
            if (!graph[i][j].hasWall(wall)) {
                continue;
            }
            Vector2 dir = wall.getRelativePosition();
            int newI = (int) (i + dir.x);
            int newJ = (int) (j + dir.y);
            if (newI < 0 || newI >= MapConfig.MAP_LENGTH || newJ < 0 || newJ >= MapConfig.MAP_LENGTH || visited[newI][newJ]) {
                continue;
            }
            graph[i][j].removeWall(wall);
            graph[newI][newJ].removeWall(wall.getOppositeWall());
            dfs(graph, visited, newI, newJ);
        }
    }

    public class Node {
        private final int x, y;
        private final List<WallTypes> wallRelativePositions;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
            wallRelativePositions = new ArrayList<>();
        }

        void addWall(WallTypes wall) {
            wallRelativePositions.add(wall);
        }

        void removeWall(WallTypes wall) {
            wallRelativePositions.remove(wall);
        }

        public boolean hasWall(WallTypes wall) {
            return wallRelativePositions.contains(wall);
        }

        @Override
        public String toString() {
            StringBuilder res = new StringBuilder("[");
            for (int i = 0; i < wallRelativePositions.size(); i++) {
                Vector2 relativePos = wallRelativePositions.get(i).getRelativePosition();
                res.append(WallTypes.valueOfRelativePos(relativePos));
                if (i != wallRelativePositions.size() - 1) {
                    res.append(",");
                }
            }
            res.append("]");
            return res.toString();
        }

    }
}


