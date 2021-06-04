package connection.state;

import java.util.Map;

import connection.Client;

public interface StateClient extends Client {
    interface ServerResponseHandler {
        void updatePoints(Map<String, Integer> points);
        void updateCountdown(int time);
        void endGame(Map<String, Integer> points);
    }

    void dispatchMessages(ServerResponseHandler handler);
}
