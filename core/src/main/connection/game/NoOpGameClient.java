package connection.game;

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
    public void syncState(long sequenceNumber, PlayerInput playerInput) {

    }
}
