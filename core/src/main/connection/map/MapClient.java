package connection.map;

import connection.Client;
import util.Point2D;

public interface MapClient extends Client {
    interface ServerResponseHandler {
        void displayAdminUI();

        void startGame(int mapLength, int seed, boolean isHost);

        void updateMap(int mapLength, int seed);

        void updateInitialPosition(Point2D position);
    }

    void syncState(int mapLength, int seed, boolean gameStarted);

    void dispatchMessages(ServerResponseHandler responseHandler);

    interface ServerResponseNameHandler {
        void updateName(String name);
    }

    void updateName(ServerResponseNameHandler handler);
}
