package map.generator;


import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import map.config.MapConfig;
import types.WallTypes;

public class MapGenerator {
    private Node[][] graph;

    public MapGenerator() {
        int map_length = MapConfig.MAP_LENGTH;
        graph = new Node[map_length][map_length];
        for (int i = 0; i < map_length; i++) {
            for (int j = 0; j < map_length; j++) {
                graph[i][j] = new Node(i, j);
                graph[i][j].addWall(new Vector2(-1, 0));
                graph[i][j].addWall(new Vector2(1, 0));
                graph[i][j].addWall(new Vector2(0, -1));
                graph[i][j].addWall(new Vector2(0, 1));
            }
        }
    }

    public Node[][] generateMap() {
        return graph;
    }

    public class Node {
        private final int x, y;
        private final List<Vector2> wallRelativePositions;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
            wallRelativePositions = new ArrayList<>();
        }

        void addWall(Vector2 direction) {
            wallRelativePositions.add(direction);
        }

        @Override
        public String toString() {
            StringBuilder res = new StringBuilder("[");
            for (int i = 0; i < wallRelativePositions.size(); i++) {
                Vector2 relativePos = wallRelativePositions.get(i);
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


