package map.generator;


import com.badlogic.gdx.math.Vector2;

import map.config.MapConfig;

public class MapGenerator {
    private Node[][] graph;

    public MapGenerator() {
        int map_length = MapConfig.MAP_LENGTH;
        graph = new Node[map_length][map_length];
        for (int i = 0; i < map_length; i++) {
            for (int j = 0; j < map_length; j++) {
                graph[i][j] = new Node(i, j);
                graph[i][j].removeEdge(new Vector2(-1, 0));
                graph[i][j].removeEdge(new Vector2(1, 0));
                graph[i][j].removeEdge(new Vector2(0, -1));
                graph[i][j].removeEdge(new Vector2(0, 1));
            }
        }
    }

    public Node[][] generateMap() {
        return graph;
    }
}


