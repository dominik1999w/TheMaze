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
        //System.out.println(chosenGenerator);
    }

    public Map generateMap(int seed) {
        this.random = new Random(seed);

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

    class Edge {
        Point2Di u;
        Point2Di v;

        Edge(Point2Di u, Point2Di v){
            this.u = new Point2Di(u);
            this.v = new Point2Di(v);
        }
    }

    private ArrayList<Edge> primMST(ArrayList<Point2Di> pivots) {
        float[][] adj = new float[pivots.size()][pivots.size()];
        int P = pivots.size();

        for (int i = 0; i < P; i++) {
            for (int j = 0; j < P; j++) {
                adj[i][j] = Point2D.dist(new Point2D(pivots.get(i).x(), pivots.get(i).y()), new Point2D(pivots.get(j).x(), pivots.get(j).y()));
            }
        }

        int[] parent = new int[P];
        float[] key = new float[P];
        Boolean[] mstSet = new Boolean[P];
        for (int i = 0; i < P; i++) {
            key[i] = Integer.MAX_VALUE;
            mstSet[i] = false;
        }

        key[0] = 0;
        parent[0] = -1;
        for (int count = 0; count < P - 1; count++) {
            int u = -1;
            float min = Float.MAX_VALUE;

            for (int v = 0; v < P; v++) {
                if (!mstSet[v] && key[v] < min) {
                    min = key[v];
                    u = v;
                }
            }

            mstSet[u] = true;
            for (int v = 0; v < P; v++) {
                if (adj[u][v] != 0 && !mstSet[v] && adj[u][v] < key[v]) {
                    parent[v] = u;
                    key[v] = adj[u][v];
                }
            }
        }

        ArrayList<Edge> result = new ArrayList<>();
        for (int i = 1; i < P; i++) {
            result.add(new Edge(pivots.get(i), pivots.get(parent[i])));

             ; // indexes of first and second point
            //System.out.println(parent[i] + " - " + i + "\t" + adj[i][parent[i]]);
        }
        return result;
    }

    static boolean onSegment(Point2Di p, Point2Di q, Point2Di r) {
        if (q.x() <= Math.max(p.x(), r.x()) && q.x() >= Math.min(p.x(), r.x()) &&
                q.y() <= Math.max(p.y(), r.y()) && q.y() >= Math.min(p.y(), r.y())) {
            return true;
        }
        else {
            return false;
        }
    }

    static int orientation(Point2Di p, Point2Di q, Point2Di r) {
        int val = (q.y() - p.y()) * (r.x() - q.x()) -(q.x() - p.x()) * (r.y() - q.y());

        if (val == 0) {
            return 0;
        }
        return (val > 0)? 1: 2;
    }

    private static boolean doIntersect(Point2Di p1, Point2Di q1, Point2Di p2, Point2Di q2) {
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        if (o1 != o2 && o3 != o4) return true;
        if (o1 == 0 && onSegment(p1, p2, q1)) return true;
        if (o2 == 0 && onSegment(p1, q2, q1)) return true;
        if (o3 == 0 && onSegment(p2, p1, q2)) return true;
        if (o4 == 0 && onSegment(p2, q1, q2)) return true;

        return false;
    }

    private Map generateCaves(int seed) {
        float pivotFill = 0.1f * (float) length * length;
        int randomEdges = (int)Math.sqrt(pivotFill/2) + 1;

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                graph[i][j] = new Map.Node(i, j, new ArrayList<>(Arrays.asList(WallType.values())));
            }
        }

        int[][] grid = new int[length][length];

        ArrayList<Point2Di> pivots = new ArrayList<Point2Di>();
        for (int i = 0; i < pivotFill; i++) {
            pivots.add(new Point2Di((random.nextInt() & Integer.MAX_VALUE) % length, (random.nextInt() & Integer.MAX_VALUE) % length));
        }

        for (Point2Di p : pivots) {
            grid[p.x()][p.y()] = 1;
        }

        ArrayList<Edge> edges = primMST(pivots);

        for (int i = 0; i < randomEdges;){
            Point2Di u = pivots.get((random.nextInt() & Integer.MAX_VALUE) % pivots.size());
            Point2Di v = pivots.get((random.nextInt() & Integer.MAX_VALUE) % pivots.size());
            if (u == v){
                continue;
            }

            Edge uv = new Edge(u, v);
            if (edges.contains(uv)) {
                continue;
            }

            boolean intersect = false;
            for(Edge e : edges) {

                if (!u.equals(e.u) && !u.equals(e.v) && !v.equals(e.u) && !v.equals(e.v)) {
                    if(doIntersect(u, v, e.u, e.v)) {
                        intersect = true;
                        break;
                    }
                }
            }
            if (!intersect) {
                edges.add(uv);
                i++;
            }
        }

        for (Edge e : edges) {
            float p1x = e.u.x();
            float p1y = e.u.y();
            float p2x = e.v.x();
            float p2y = e.v.y();

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


