package map.generator;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import map.Map;
import types.WallType;
import util.Point2D;
import util.Point2Di;

import static types.WallType.DOWN_WALL;
import static types.WallType.LEFT_WALL;
import static types.WallType.RIGHT_WALL;
import static types.WallType.UP_WALL;

public class MapGenerator {
    private final Map.Node[][] graph;
    private Random random;
    private final float walls_to_remove = 0.5f;
    private final int length;
    static int chosenGenerator = 1;

    public MapGenerator(int length) {
        this.length = length;
        graph = new Map.Node[length][length];
    }

    public static void chooseGenerator(int gen) {
        chosenGenerator = gen;
        System.out.println(chosenGenerator);
    }

    public Map generateMap(int seed) {
        if (chosenGenerator == 1) {
            return generateRandomMap(seed);
        }
        else if (chosenGenerator == 2) {
            return generateCaves(seed);
        }
        else { // (chosenGenerator == 3)
            return generateCaves(seed);
        }
    }

    private Map generateCaves(int seed) {
        this.random = new Random(seed);

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                graph[i][j] = new Map.Node(i, j, new ArrayList<>(Arrays.asList(WallType.values())));
            }
        }

        float pivotFill = 0.1f;
        int[][] grid = new int[length][length];

        ArrayList<Point2Di> pivots = new ArrayList<Point2Di>();
        for (int i = 0; i < (float) length * length * pivotFill; i++) {
            pivots.add(new Point2Di((random.nextInt() & Integer.MAX_VALUE) % length, (random.nextInt() & Integer.MAX_VALUE) % length));
        }
        int V = pivots.size();

        for (Point2Di p : pivots) {
            grid[p.x()][p.y()] = 1;
        }

        class MST {
            ArrayList<Point2Di> primMST(float[][] adj) {
                int[] parent = new int[V];
                float[] key = new float[V];
                Boolean[] mstSet = new Boolean[V];
                for (int i = 0; i < V; i++) {
                    key[i] = Integer.MAX_VALUE;
                    mstSet[i] = false;
                }

                key[0] = 0;
                parent[0] = -1;
                for (int count = 0; count < V - 1; count++) {
                    int u = -1;
                    float min = Float.MAX_VALUE;

                    for (int v = 0; v < V; v++) {
                        if (!mstSet[v] && key[v] < min) {
                            min = key[v];
                            u = v;
                        }
                    }

                    mstSet[u] = true;
                    for (int v = 0; v < V; v++) {
                        if (adj[u][v] != 0 && !mstSet[v] && adj[u][v] < key[v]) {
                            parent[v] = u;
                            key[v] = adj[u][v];
                        }
                    }
                }

                ArrayList<Point2Di> result = new ArrayList<>();
                for (int i = 1; i < V; i++) {
                    result.add(new Point2Di(i, parent[i])); // indexes of first and second point
                    //System.out.println(parent[i] + " - " + i + "\t" + adj[i][parent[i]]);
                }
                return result;
            }
        }

        MST t = new MST();
        float[][] adj = new float[pivots.size()][pivots.size()];
        for (int i = 0; i < pivots.size(); i++) {
            for (int j = 0; j < pivots.size(); j++) {
                adj[i][j] = Point2D.dist(new Point2D(pivots.get(i).x(), pivots.get(i).y()), new Point2D(pivots.get(j).x(), pivots.get(j).y()));
            }
        }

        ArrayList<Point2Di> edges = t.primMST(adj);
        //for (int i = 0; i < edges.size(); i++) System.out.println("(" + pivots.get(edges.get(i).x()).x()+ "," + pivots.get(edges.get(i).x()).y() + ") (" + pivots.get(edges.get(i).y()).x()+ "," + pivots.get(edges.get(i).y()).y() + ")");


        for (Point2Di e : edges) {
            float p1x = pivots.get(e.x()).x();
            float p1y = pivots.get(e.x()).y();
            float p2x = pivots.get(e.y()).x();
            float p2y = pivots.get(e.y()).y();

            float dx = Math.signum(p2x - p1x);
            float dy = Math.signum(p2y - p1y);
            if (p1x == p2x || p1y == p2y) {
                while (p1x != p2x || p1y != p2y) {
                    p1x += dx;
                    p1y += dy;
                    grid[(int) p1x][(int) p1y] = 1;
                }
            } else {
                if (Math.abs(p2x - p1x) < Math.abs(p2y - p1y)) {
                    dx = (p2x - p1x) / (p2y - p1y) * dy;
                } else {
                    dy = (p2y - p1y) / (p2x - p1x) * dx;
                }
                while (p1x != p2x && p1y != p2y) {
                    grid[Math.round(p1x)][Math.round(p1y)] = 1;
                    grid[Math.round(p2x)][Math.round(p2y)] = 1;

                    p1x += dx;
                    p1y += dy;
                }
            }
        }


        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if (grid[i][j] == 1) {
                    if (j > 0) graph[i][j].removeWall(DOWN_WALL);
                    if (j - 1 >= 0) graph[i][j - 1].removeWall(UP_WALL);

                    if (i > 0) graph[i][j].removeWall(LEFT_WALL);
                    if (i - 1 >= 0) graph[i - 1][j].removeWall(RIGHT_WALL);

                    if (j < length - 1) graph[i][j].removeWall(UP_WALL);
                    if (j + 1 < length) graph[i][j + 1].removeWall(DOWN_WALL);

                    if (i < length - 1) graph[i][j].removeWall(RIGHT_WALL);
                    if (i + 1 < length) graph[i + 1][j].removeWall(LEFT_WALL);
                }
            }
        }

        for (int k = 0; k < 4; k++) {
            for (int i = 0; i < length; i++) { // horizontal walls
                for (int j = 0; j < length - 1; j++) {
                    if (graph[i][j].hasWall(UP_WALL)) {
                        boolean leftSide = graph[i][j].hasWall(LEFT_WALL) || graph[i][j + 1].hasWall(LEFT_WALL);
                        boolean rightSide = graph[i][j].hasWall(RIGHT_WALL) || graph[i][j + 1].hasWall(RIGHT_WALL);
                        if (!leftSide || !rightSide) {
                            graph[i][j].removeWall(UP_WALL);
                            graph[i][j+1].removeWall(DOWN_WALL);
                        }
                    }

                }
            }

            for (int i = 0; i < length - 1; i++) { // vertical walls
                for (int j = 0; j < length; j++) {
                    if (graph[i][j].hasWall(RIGHT_WALL)) {
                        boolean upSide = graph[i][j].hasWall(UP_WALL) || graph[i + 1][j].hasWall(UP_WALL);
                        boolean downSide = graph[i][j].hasWall(DOWN_WALL) || graph[i + 1][j].hasWall(DOWN_WALL);
                        if (!upSide || !downSide) {
                            graph[i][j].removeWall(RIGHT_WALL);
                            graph[i + 1][j].removeWall(LEFT_WALL);
                        }
                    }
                }
            }
        }

        return new Map(graph);
    }


    private Map generateRandomMap(int seed) {
        this.random = new Random(seed);
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                graph[i][j] = new Map.Node(i, j, new ArrayList<>(Arrays.asList(WallType.values())));
            }
        }
        boolean[][] visited = new boolean[length][length];
        dfs(graph, visited, 0, 0);

        for (int i = 1; i < length; i++) {
            for (int j = 1; j < length; j++) {
                if (random.nextDouble() < walls_to_remove) {
                    graph[i][j].removeWall(DOWN_WALL);
                    graph[i][j - 1].removeWall(UP_WALL);
                }
                if (random.nextDouble() < walls_to_remove) {
                    graph[i][j].removeWall(LEFT_WALL);
                    graph[i - 1][j].removeWall(RIGHT_WALL);
                }
            }
        }

        return new Map(graph);
    }

    private void dfs(Map.Node[][] graph, boolean[][] visited, int i, int j) {
        visited[i][j] = true;
        List<WallType> wallList = Arrays.asList(WallType.values());
        Collections.shuffle(wallList, random);

        for (WallType wall : wallList) {
            if (!graph[i][j].hasWall(wall)) {
                continue;
            }

            int newI = i + wall.getRelativePositionX();
            int newJ = j + wall.getRelativePositionY();
            if (newI < 0 || newI >= length || newJ < 0 || newJ >= length || visited[newI][newJ]) {
                continue;
            }

            graph[i][j].removeWall(wall);
            graph[newI][newJ].removeWall(wall.getOppositeWall());
            dfs(graph, visited, newI, newJ);
        }
    }
}


