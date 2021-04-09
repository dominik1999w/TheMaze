package types;

import util.Point2D;
import util.Shape2D;

public enum WallType {
    UP_WALL(0, 1) {
        @Override
        public Shape2D getWallShape(Shape2D shape) {
            float positionX = shape.getPosition().x;
            float positionY = shape.getPosition().y + 1;
            float thickness = shape.getSize().x + shape.getSize().y;
            float length = shape.getSize().y;
            return new Shape2D(new Point2D(positionX, positionY), new Point2D(thickness, length));
        }

        @Override
        public WallType getOppositeWall() {
            return DOWN_WALL;
        }
    },

    DOWN_WALL(0, -1) {
        @Override
        public Shape2D getWallShape(Shape2D shape) {
            float positionX = shape.getPosition().x;
            float positionY = shape.getPosition().y;
            float thickness = shape.getSize().x + shape.getSize().y;
            float length = shape.getSize().y;
            return new Shape2D(new Point2D(positionX, positionY), new Point2D(thickness, length));
        }

        @Override
        public WallType getOppositeWall() {
            return UP_WALL;
        }
    },

    RIGHT_WALL(1, 0) {
        @Override
        public Shape2D getWallShape(Shape2D shape) {
            float positionX = shape.getPosition().x + 1;
            float positionY = shape.getPosition().y;
            float thickness = shape.getSize().y;
            float length = shape.getSize().x;
            return new Shape2D(new Point2D(positionX, positionY), new Point2D(thickness, length));
        }

        @Override
        public WallType getOppositeWall() {
            return LEFT_WALL;
        }
    },

    LEFT_WALL(-1, 0) {
        @Override
        public Shape2D getWallShape(Shape2D shape) {
            float positionX = shape.getPosition().x;
            float positionY = shape.getPosition().y;
            float thickness = shape.getSize().y;
            float length = shape.getSize().x;
            return new Shape2D(new Point2D(positionX, positionY), new Point2D(thickness, length));
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

    public abstract Shape2D getWallShape(Shape2D shape);

    public abstract WallType getOppositeWall();
}
