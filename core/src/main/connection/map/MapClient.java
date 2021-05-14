package connection.map;

import connection.Client;
import util.Point2D;

public interface MapClient extends Client {
    void syncState();
    boolean isHost();
    int getMapLength();
    void setMapLength(int value);
    int getSeed();
    void setSeed(int seed);
    boolean isGameStarted();
    void setGameStarted(boolean gameStarted);
    Point2D getStartPos();
}
