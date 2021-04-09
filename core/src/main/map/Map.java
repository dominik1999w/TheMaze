package map;

import java.util.List;

import types.WallType;

public class Map {
    private final Node[][] map;

    public Map(Node[][] map) {
        this.map = map;
    }


    public boolean hasWall(WallType wall, int x, int y) {
        if (x < 0 || y < 0 || x >= map.length || y >= map[0].length) {
            return true;
        }
        return map[x][y].hasWall(wall);
    }

    public Node[][] getMapArray() {
        return map;
    }

    public static class Node {
        private final List<WallType> wallRelativePositions;
        private final int positionX;
        private final int positionY;

        public Node(int x, int y, List<WallType> walls) {
            this.positionX = x;
            this.positionY = y;
            wallRelativePositions = walls;
        }

        public void addWall(WallType wall) {
            wallRelativePositions.add(wall);
        }

        public void removeWall(WallType wall) {
            wallRelativePositions.remove(wall);
        }

        public boolean hasWall(WallType wall) {
            return wallRelativePositions.contains(wall);
        }

        public List<WallType> getWallRelativePositions() {
            return wallRelativePositions;
        }

        public int getPositionX() {
            return positionX;
        }

        public int getPositionY() {
            return positionY;
        }

        @Override
        public String toString() {
            StringBuilder res = new StringBuilder("[");
            for (int i = 0; i < wallRelativePositions.size(); i++) {
                int relativePosX = wallRelativePositions.get(i).getRelativePositionX();
                int relativePosY = wallRelativePositions.get(i).getRelativePositionY();

                res.append(WallType.valueOfRelativePos(relativePosX, relativePosY));
                if (i != wallRelativePositions.size() - 1) {
                    res.append(",");
                }
            }
            res.append("]");
            return res.toString();
        }

    }
}
