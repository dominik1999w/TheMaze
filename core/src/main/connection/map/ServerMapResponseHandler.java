package connection.map;

import util.Point2D;

public interface ServerMapResponseHandler {
    void displayAdminUI();

    void startGame(int mapLength, int seed, boolean isHost);

    void updateMap(int mapLength, int seed);

    void updateInitialPosition(Point2D position);
}
