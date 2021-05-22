package connection.map;

import connection.Client;

public interface MapClient extends Client {
    void syncState(int mapLength, int seed, boolean gameStarted);

    void dispatchMessages(ServerMapResponseHandler responseHandler);
}
