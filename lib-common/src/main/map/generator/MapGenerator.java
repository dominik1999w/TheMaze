package map.generator;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
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

    public MapGenerator(int length) {
        this.length = length;
        graph = new Map.Node[length][length];
    }

    public Map generateMap(int seed, int generatorType) {
        generateGraph(seed, generatorType);
        return new Map(graph);
    }

    private void generateGraph(int seed, int generatorType) {
        this.random = new Random(seed);

        if (generatorType == 1) {
            generateRandomMap();
        }
        else if (generatorType == 2) {
            generateCaves();
        }
        else if (generatorType == 3) {
            generateDungeon();
        }
        else {
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < length; j++) {
                    graph[i][j] = new Map.Node(i, j, new ArrayList<>(Arrays.asList(WallType.values())));
                }
            }
        }
    }

    public boolean[][] getSpawnMap(int seed, int generatorType){
        generateGraph(seed, generatorType);
        boolean[][] spawnMap = new boolean[length][length];
        for(int i = 0; i<length; i++) {
            for(int j = 0; j<length; j++) {
                if (!graph[i][j].hasWall(UP_WALL) ||
                        !graph[i][j].hasWall(DOWN_WALL) ||
                        !graph[i][j].hasWall(RIGHT_WALL) ||
                        !graph[i][j].hasWall(LEFT_WALL)) {
                    spawnMap[i][j] = true;
                }
                else {
                    spawnMap[i][j] = false;
                }
            }
        }
        return spawnMap;
    }


    class Edge {
        Point2Di u;
        Point2Di v;

        Edge(Point2Di u, Point2Di v){
            this.u = new Point2Di(u);
            this.v = new Point2Di(v);
        }
    }


    class Path {
        int from;
        ArrayList<Point2Di> steps;
        int to;

        Path(int from, ArrayList<Point2Di> steps, int to) {
            this.from = from;
            this.steps = steps;
            this.to = to;
        }
    }

    class Room{
        int index;
        int x;
        int y;
        int w;
        int h;
        ArrayList<Path> paths = new ArrayList<>();

        Room(int index, int x, int y, int w, int h){
            this.index = index;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private void findRoomsOnLine(int beginX, int beginY, int endX, int endY, int[][] grid, ArrayList<Room> rooms) {
        int lastRoomIndex = -1;
        boolean legitLine = false;
        int numberOfBorders = 0;

        int i = beginX;
        int j = beginY;
        int di = (int)Math.signum(endX - beginX);
        int dj = (int)Math.signum(endY - beginY);

        ArrayList<Point2Di> pathSteps = new ArrayList<>();

        for(;i != endX || j != endY; i+=di, j+=dj) {
            if (grid[i][j] >= 0) { // room
                if (legitLine && grid[i][j] != lastRoomIndex) {
                    //rooms.get(grid[i][j]).paths.add(new Path(lastRoomIndex, pathSteps, grid[i][j])); // reverse it probably
                    rooms.get(lastRoomIndex).paths.add(new Path(lastRoomIndex, pathSteps, grid[i][j]));
                    pathSteps = new ArrayList<>();
                }
                lastRoomIndex = grid[i][j];
                legitLine = true;
                numberOfBorders = 0;
            }
            else if(grid[i][j] == -1) { // empty
                if (legitLine) {
                    pathSteps.add(new Point2Di(i, j));
                }
            }
            else { // room border
                if (legitLine) {
                    numberOfBorders++;

                    if (numberOfBorders > 2) {
                        legitLine = false;
                        pathSteps = new ArrayList<>();
                    }
                    else {
                        pathSteps.add(new Point2Di(i, j));
                    }
                }
            }
        }
    }

    private Map generateDungeon() {
        int numberOfRooms = (int) length + 1;
        int minRoomLength = (int) length / 14 + 2;
        int maxRoomLength = (int) length / 7 + 2;
        int maxNumberOfTries = 100;
        int longPathRemove = (int) Math.sqrt(length) + 2;
        // -1=empty, -2=room_border, 999=paths, non-negative=rooms
        int[][] grid = new int[length][length];

        for(int i=0 ; i<length; i++) {
            for(int j=0; j<length; j++) {
                grid[i][j] = -1;
            }
        }

        ArrayList<Room> rooms = new ArrayList<>();
        for(int r = 0, tries = 0; r < numberOfRooms; tries++){
            if (tries >= maxNumberOfTries) {
                break;
            }

            int w = random.nextInt(maxRoomLength - minRoomLength + 1) + minRoomLength;
            int h = random.nextInt(maxRoomLength - minRoomLength + 1) + minRoomLength;
            int x = random.nextInt(length - w + 1);
            int y = random.nextInt(length - h + 1);

            boolean legitRoom = true;
            for (int i = x; i < x + w && legitRoom; i++) {
                for (int j = y; j < y + h; j++) {
                    if (grid[i][j] != -1) {
                        legitRoom = false;
                        break;
                    }
                }
            }

            if (legitRoom) {
                for (int i = Math.max(0, x - 1); i < Math.min(x + w + 1, length); i++) { // can be optimized (borders only)
                    for(int j = Math.max(0, y - 1); j < Math.min(y + h + 1, length); j++) {
                        grid[i][j] = -2;
                    }
                }

                for(int i = x; i<x+w; i++) {
                    for(int j=y; j<y+h; j++) {
                        grid[i][j] = r;
                    }
                }

                rooms.add(new Room(r, x, y, w, h));
                r++;
                tries = 0;
            }
        }

        for(int i=0; i<length; i+=2) { // reverse lists?
            findRoomsOnLine(i, 0, i, length, grid, rooms);
            findRoomsOnLine(0, i, length, i, grid, rooms);
            //findRoomsOnLine(i, length - 1, i, -1, grid, rooms);
            //findRoomsOnLine(length - 1, i,-1, i, grid, rooms);
        }

        for (Room r : rooms) {
            Iterator itr = r.paths.iterator();
            while (itr.hasNext()) {
                int len = ((Path)itr.next()).steps.size();
                if (len > longPathRemove)
                    itr.remove();
            }
        }

        for (Room r : rooms) {
            r.paths.sort((o1, o2) -> o1.to - o2.to);
        }

        for (Room r : rooms) {
            for (int i = 0; i < r.paths.size(); i++) {
                int j = i + 1;
                for (; j < r.paths.size(); j++) {
                    if (r.paths.get(i).to != r.paths.get(j).to) {
                        break;
                    }
                }
                // [i,j-1]
                ArrayList<Point2Di> path = r.paths.get(random.nextInt(j - i) + i).steps;
                for (Point2Di p : path) {
                    grid[p.x()][p.y()] = 99999;
                }
                i = j - 1;
            }
        }

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                graph[i][j] = new Map.Node(i, j, new ArrayList<>(Arrays.asList(WallType.values())));
            }
        }

        for(int i=0 ; i < length; i++) {
            for(int j=0; j < length; j++) {
                if (grid[i][j] >= 0) {
                    grid[i][j] = 0; // empty
                }
                else {
                    grid[i][j] = 1; // walls
                }
            }
        }

        int currentIndex = 2;
        int biggestIndex = -1;
        int biggestSize = -1;

        for(int i = 0 ; i < length; i++) {
            for(int j = 0; j < length; j++) {
                if (grid[i][j] == 0) {
                    int size = findBiggestDfs(grid, i, j, currentIndex);
                    if (size > biggestSize) {
                        biggestIndex = currentIndex;
                        biggestSize = size;
                    }
                    currentIndex++;
                }
            }
        }

        for(int i=0 ; i < length; i++) {
            for(int j=0; j < length; j++) {
                if (grid[i][j] == biggestIndex) {
                    grid[i][j] = 0; // empty
                }
                else {
                    grid[i][j] = 1; // walls
                }
            }
        }

        for (int i = 0; i < length; i++) { // horizontal walls
            for (int j = 0; j < length - 1; j++) {
                if (grid[i][j] == 0 && grid[i][j + 1] == 0) {
                    graph[i][j].removeWall(UP_WALL);
                    graph[i][j + 1].removeWall(DOWN_WALL);
                }
            }
        }
        for (int i = 0; i < length - 1; i++) { // vertical walls
            for (int j = 0; j < length; j++) {
                if (grid[i][j] == 0 && grid[i + 1][j] == 0) {
                    graph[i][j].removeWall(RIGHT_WALL);
                    graph[i + 1][j].removeWall(LEFT_WALL);
                }
            }
        }

        return new Map(graph);
    }

    int findBiggestDfs(int[][] grid, int i, int j, int currentIndex) {
        if (i<0 || j<0 || i>=length || j>=length || grid[i][j] != 0) {
            return 0;
        }
        grid[i][j] = currentIndex;

        return 1 +
                findBiggestDfs(grid, i - 1, j, currentIndex) +
                findBiggestDfs(grid, i, j - 1, currentIndex) +
                findBiggestDfs(grid, i + 1, j, currentIndex) +
                findBiggestDfs(grid, i, j + 1, currentIndex);
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
        }
        return result;
    }

    private static boolean onSegment(Point2Di p, Point2Di q, Point2Di r) {
        if (q.x() <= Math.max(p.x(), r.x()) && q.x() >= Math.min(p.x(), r.x()) &&
                q.y() <= Math.max(p.y(), r.y()) && q.y() >= Math.min(p.y(), r.y())) {
            return true;
        }
        else {
            return false;
        }
    }

    private static int orientation(Point2Di p, Point2Di q, Point2Di r) {
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


    private void generateCaves() {
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
    }

    private void generateRandomMap() {
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


