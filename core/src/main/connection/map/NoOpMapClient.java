package connection.map;

import java.util.UUID;

public class NoOpMapClient implements MapClient {
    @Override
    public void disconnect() {

    }

    @Override
    public void syncState(int mapLength, int seed, boolean gameStarted) {

    }

    @Override
    public void dispatchMessages(ServerMapResponseHandler responseHandler) {

    }


    @Override
    public void connect(UUID id) {

    }
}
