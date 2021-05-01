package connection;

import java.util.UUID;

import entity.player.PlayerInput;

public class NoOpGameClient implements GameClient {
    @Override
    public void connect() {
    }

    @Override
    public void dispatchMessages(ServerResponseHandler responseHandler) {

    }

    @Override
    public void enterGame(UUID id) {

    }

    @Override
    public boolean syncState(long sequenceNumber, PlayerInput playerInput) {
        return true;
    }
}
