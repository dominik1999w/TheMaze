package connection.map;

import java.util.Map;

import connection.Client;
import util.Point2D;

public interface MapClient extends Client {
    interface ServerResponseHandler {
        void displayAdminUI();

        void startGame(int mapLength, int seed, int generatorType, boolean isHost);

        void updateMap(int mapLength, int seed, int generatorType);

        void updateInitialPosition(Point2D position);

        void updateClientsNames(Map<String, String> names);
    }

    void syncState(int mapLength, int seed, int generatorType, boolean gameStarted);

    void dispatchMessages(ServerResponseHandler responseHandler);

    String getUserName();
}