package connection;

import lib.connection.ConnectReply;

public interface GameClient {

    ConnectReply connect();
    void syncGameState();

}
