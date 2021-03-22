package types;

import com.badlogic.gdx.math.Vector2;

import map.containers.MapWall;

public enum WallTypes {
    UP_WALL(new Vector2(-1, 0)) {
        @Override
        public MapWall createWall(int tileSize, float x, float y, int thickness) {
            Vector2 position = new Vector2(x, y + 1);
            Vector2 size = new Vector2(tileSize, thickness);
            return new MapWall(position, size);
        }
    },

    DOWN_WALL(new Vector2(1, 0)) {
        @Override
        public MapWall createWall(int tileSize, float x, float y, int thickness) {
            Vector2 position = new Vector2(x, y);
            Vector2 size = new Vector2(tileSize, thickness);
            return new MapWall(position, size);
        }
    },

    RIGHT_WALL(new Vector2(0, 1)) {
        @Override
        public MapWall createWall(int tileSize, float x, float y, int thickness) {
            Vector2 position = new Vector2(x + 1, y);
            Vector2 size = new Vector2(thickness, tileSize);
            return new MapWall(position, size);
        }
    },

    LEFT_WALL(new Vector2(0, -1)) {
        @Override
        public MapWall createWall(int tileSize, float x, float y, int thickness) {
            Vector2 position = new Vector2(x, y);
            Vector2 size = new Vector2(thickness, tileSize);
            return new MapWall(position, size);
        }
    };

    private final Vector2 relativePosition;

    WallTypes(Vector2 relativePosition) {
        this.relativePosition = relativePosition;
    }

    public static WallTypes valueOfRelativePos(Vector2 relativePos) {
        for (WallTypes w : values()) {
            if (w.relativePosition.equals(relativePos)) {
                return w;
            }
        }
        return null;
    }

    public abstract MapWall createWall(int tileSize, float x, float y, int thickness);
}
