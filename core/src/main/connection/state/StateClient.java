package connection.state;

import connection.Client;

public interface StateClient extends Client {
    interface ServerResponseHandler {
        void showGameCountdown(float delta);
    }

    void dispatchMessages(ServerResponseHandler handler);
}
