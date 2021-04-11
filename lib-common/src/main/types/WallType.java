package types;

public enum WallType {
    UP_WALL(0, 1) {
        @Override
        public WallShape getWallShape(int positionX, int positionY, int sizeX, int sizeY) {
            return new WallShape(positionX, positionY + 1, sizeX + sizeY, sizeY);
        }

        @Override
        public WallType getOppositeWall() {
            return DOWN_WALL;
        }
    },

    DOWN_WALL(0, -1) {
        @Override
        public WallShape getWallShape(int positionX, int positionY, int sizeX, int sizeY) {
            return new WallShape(positionX, positionY, sizeX + sizeY, sizeY);
        }

        @Override
        public WallType getOppositeWall() {
            return UP_WALL;
        }
    },

    RIGHT_WALL(1, 0) {
        @Override
        public WallShape getWallShape(int positionX, int positionY, int sizeX, int sizeY) {
            return new WallShape(positionX + 1, positionY, sizeY, sizeX);
        }

        @Override
        public WallType getOppositeWall() {
            return LEFT_WALL;
        }
    },

    LEFT_WALL(-1, 0) {
        @Override
        public WallShape getWallShape(int positionX, int positionY, int sizeX, int sizeY) {
            return new WallShape(positionX, positionY, sizeY, sizeX);
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

    public abstract WallShape getWallShape(int positionX, int positionY, int sizeX, int sizeY);

    public abstract WallType getOppositeWall();

    public static class WallShape {
        private final int positionX;
        private final int positionY;
        private final int sizeX;
        private final int sizeY;

        public WallShape(int positionX, int positionY, int sizeX, int sizeY) {
            this.positionX = positionX;
            this.positionY = positionY;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
        }

        public int getPositionX() {
            return positionX;
        }

        public int getPositionY() {
            return positionY;
        }

        public int getSizeX() {
            return sizeX;
        }

        public int getSizeY() {
            return sizeY;
        }
    }
}
