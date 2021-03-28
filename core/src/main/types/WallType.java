package types;

import map.containers.MapWall;

public enum WallType {
    UP_WALL(0, 1) {
        @Override
        public MapWall createWall(int tileSize, int positionX, int positionY, int thickness) {
            return new MapWall(positionX, positionY + 1, tileSize + thickness, thickness);
        }

        @Override
        public WallType getOppositeWall() {
            return DOWN_WALL;
        }
    },

    DOWN_WALL(0, -1) {
        @Override
        public MapWall createWall(int tileSize, int positionX, int positionY, int thickness) {
            return new MapWall(positionX, positionY, tileSize + thickness, thickness);
        }

        @Override
        public WallType getOppositeWall() {
            return UP_WALL;
        }
    },

    RIGHT_WALL(1, 0) {
        @Override
        public MapWall createWall(int tileSize, int positionX, int positionY, int thickness) {
            return new MapWall(positionX + 1, positionY, thickness, tileSize);
        }

        @Override
        public WallType getOppositeWall() {
            return LEFT_WALL;
        }
    },

    LEFT_WALL(-1, 0) {
        @Override
        public MapWall createWall(int tileSize, int positionX, int positionY, int thickness) {
            return new MapWall(positionX, positionY, thickness, tileSize);
        }

        @Override
        public WallType getOppositeWall() {
            return RIGHT_WALL;
        }
    };

    private final int relativePositionX;
    private final int relativePositionY;

    WallType(int relativePositionX, int relativePositionY) {
        this.relativePositionX = relativePositionX;
        this.relativePositionY = relativePositionY;
    }

    public static WallType valueOfRelativePos(int relativePosX, int relativePosY) {
        for (WallType w : values()) {
            if (w.relativePositionX == relativePosX && w.relativePositionY == relativePosY) {
                return w;
            }
        }
        return null;
    }

    public int getRelativePositionX() {
        return relativePositionX;
    }

    public int getRelativePositionY() {
        return relativePositionY;
    }

    public abstract MapWall createWall(int tileSize, int positionX, int positionY, int thickness);

    public abstract WallType getOppositeWall();
}
