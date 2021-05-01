package connection;

import java.util.UUID;

import entity.player.PlayerInput;

public interface GameClient extends Client {
    void dispatchMessages(ServerResponseHandler responseHandler);
    void enterGame(UUID id);
    boolean syncState(long sequenceNumber, PlayerInput playerInput);
}
