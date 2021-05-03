package connection.game;

import java.util.UUID;

import connection.Client;
import entity.player.PlayerInput;

public interface GameClient extends Client {
    void dispatchMessages(ServerResponseHandler responseHandler);
    void syncState(long sequenceNumber, PlayerInput playerInput);
}
