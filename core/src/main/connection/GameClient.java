package connection;

import java.util.UUID;

import entity.player.PlayerInput;

public interface GameClient extends Client {
    void enterGame(UUID id, ServerResponseListener responseListener);
    boolean syncState(long sequenceNumber, PlayerInput playerInput);
}
