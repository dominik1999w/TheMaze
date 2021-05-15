package connection.map;

import java.util.UUID;

import map.MapConfig;
import util.Point2D;

public class NoOpMapClient implements MapClient {
    @Override
    public boolean isHost() {
        return false;
    }

    @Override
    public int getMapLength() {
        return 5;
    }

    @Override
    public void setMapLength(int value) {

    }

    @Override
    public int getSeed() {
        return 0;
    }

    @Override
    public void setSeed(int seed) {

    }

    @Override
    public boolean isGameStarted() {
        return true;
    }

    @Override
    public void setGameStarted(boolean gameStarted) {

    }

    @Override
    public Point2D getStartPos() {
        return new Point2D(3.5f * MapConfig.BOX_SIZE, 2.5f * MapConfig.BOX_SIZE);
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void syncState() {

    }

    @Override
    public void connect(UUID id) {

    }
}
