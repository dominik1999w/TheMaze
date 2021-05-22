package connection.game;

import connection.Client;
import entity.player.PlayerInput;

public interface GameClient extends Client {
    void dispatchMessages(ServerGameResponseHandler responseHandler);
    void syncState(long sequenceNumber, PlayerInput playerInput);
}
